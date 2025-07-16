from dbus_next.aio import MessageBus
from dbus_next.constants import BusType
from fastapi import FastAPI, WebSocket, WebSocketDisconnect, HTTPException
from loguru import logger as log
from pathlib import Path
from requests.exceptions import ConnectionError
from typing import List
from update_request import UpdateRequest, UpdateStatus, UpdateStatusEnum

import asyncio
import docker
import json
import requests
import time
import subprocess
from pydantic import BaseModel

# Define the paths to the versions.json files
UPDATE_DIR = Path("/usr/update")
VERSIONS_ROOT = "versions.json"
COMPOSE_ROOT = "docker-compose.yml"
FIRMWARE_ROOT = "Bundled_MMC_FWU_1.nsm"
FW_JSON_ROOT = "Bundled_MMC_FWU_1.json"
LED_FW_ROOT = "App-Ledstrip.nss"
LED_JSON_ROOT = "App-Ledstrip_1.json"
DOCKER_CREDS_ROOT = "docker_credentials.json"
BACKEND_URL = "http://localhost:7001"


class SystemUpdateRequest(BaseModel):
    update_script_name: str

# Initialize the Docker client
client = docker.DockerClient(base_url='unix://var/run/docker.sock')

# Define the FastAPI app
app = FastAPI()

logged_modules = []
journal_process = None
journal_reader_task = None
connected_websockets: List[WebSocket] = []
log_queue = asyncio.Queue(maxsize=1000)

def send_update_ready(url: str, status: bool):
    try:
        # Define the URL of the target FastAPI app
        url = url + "/receive_update"

        # Define the payload
        update_request = UpdateRequest(update_ready=status) 
        payload = update_request.dict()

        # Send the POST request
        response = requests.post(url, json=payload)
        log.info(f"Response from backend: {response.json()}")
        # Return the status and response from the target API
        return {"status": response.status_code, "response": response.json()}

    except ConnectionError:
        return {"status": 200, "response" : {"do_update": True, "wait_to_update": 0}}

def send_update_status(url: str, status: UpdateStatusEnum):
    try:
        # Define the URL of the target FastAPI app
        url = url + "/update_status"

        # Create an UpdateStatus object
        update_status = UpdateStatus(update_status=status)

        # Define the payload
        payload_str = update_status.json()
        payload = json.loads(payload_str)

        # Send the POST request
        response = requests.post(url, json=payload)
        log.info(f"Response from backend: {response.json()}")
        # Return the status and response from the target API
        return {"status": response.status_code, "response": response.json()}

    except ConnectionError:
        return {"status": 500, "response": {"Message": "Unable to contact BE"}}

def docker_login():
    try:
        # Read the Docker credentials from the file
        creds = read_json_file(UPDATE_DIR / f"{DOCKER_CREDS_ROOT}")

        # Perform the login
        response = client.login(
            username=creds["Username"],
            password=creds["Password"],
            registry=creds["ServerAddress"]
        )

        log.info(f"Successfully logged in to the Docker registry: {creds['ServerAddress']}")
        return True

    except docker.errors.APIError as e:
        log.error(f"Failed to log in to the Docker registry: {e}")
        return False
    except Exception as e:
        log.error(f"Error occurred while logging in to the Docker registry: {e}")
        return False

def pull_docker_image(image_name: str) -> bool:
    try:
        log.info(f"Pulling image {image_name}...")

        # Pull the image
        image = client.images.pull(image_name)

        log.info(f"Successfully pulled image: {image.tags}")
        return True
    except docker.errors.ImageNotFound:
        log.error(f"Image {image_name} not found.")
        return False
    except docker.errors.APIError as e:
        log.error(f"Failed to pull image {image_name}: {e}")
        return False
    except Exception as e:
        log.error(f"An error occurred while pulling image {image_name}: {e}")
        return False

def read_json_file(file_path: Path):
    """Reads the specified file and returns the parsed data."""
    if file_path.exists():
        with open(file_path) as file:
            return json.load(file)
    else:
        raise FileNotFoundError(f"{file_path} not found!")

def get_image_names(versions_data):
    """Extracts the frontend and backend image names from versions data."""
    # Extract the backend and frontend info from ATC_SOFTWARE
    backend_branch_name = versions_data["ATC_SOFTWARE"]["BackendModule"]["BranchName"].replace("/", "-").replace(".", "-").lower()
    frontend_branch_name = versions_data["ATC_SOFTWARE"]["FrontendModule"]["BranchName"].replace("/", "-").replace(".", "-").lower()

    build_number = versions_data["ATC_SOFTWARE"]["BuildNumber"]
    address = read_json_file(UPDATE_DIR / f"{DOCKER_CREDS_ROOT}")["ServerAddress"]

    # Construct the image names based on branch name and build number
    fe_image = f"{address}/{frontend_branch_name}-frontend_image:{build_number}"
    be_image = f"{address}/{backend_branch_name}-backend_image:{build_number}"
    # This breaks out of a devops driven pattern until a pipeline is set up for the asset store
    as_image = f"{address}/mli-large-asset-store_image:2.0.x"


    return fe_image, be_image, as_image

def check_if_images_are_different() -> bool:
    """Checks if the images in the new versions file differ from the images in the old versions file."""
    try:
        # Read the new versions data
        new_versions_data = read_json_file(UPDATE_DIR / f"new_{VERSIONS_ROOT}")

        # Try to read the old versions data
        try:
            old_versions_data = read_json_file(UPDATE_DIR / f"current_{VERSIONS_ROOT}")

            # Get image names from both versions
            new_fe_image, new_be_image, new_as_image = get_image_names(new_versions_data)
            old_fe_image, old_be_image, old_as_image = get_image_names(old_versions_data)

            # Compare all image names at once
            images_different = (new_fe_image != old_fe_image) or (new_be_image != old_be_image) or (new_as_image != old_as_image)

            if images_different:
                log.info("Images are different between the new and old versions.")
            else:
                log.info("Images are the same in the new and old versions.")

            return images_different

        except FileNotFoundError:
            log.warning("Old versions file not found. Assuming images are different.")
            # If the old versions file doesn't exist, assume images are different
            return True

    except FileNotFoundError as e:
        log.error(f"File not found: {e}")
        # If the new versions file doesn't exist, stop the update
        return False
    except Exception as e:
        log.error(f"An error occurred while comparing images: {e}")
        # In case of any error, stop the update
        return False

async def start_service(service_name):
    try:
        # Connect to the system bus
        bus = await MessageBus(bus_type=BusType.SYSTEM).connect()

        # Get the introspection data
        introspection = await bus.introspect('org.freedesktop.systemd1', '/org/freedesktop/systemd1')

        # Get a proxy object for the systemd manager
        systemd_manager = bus.get_proxy_object('org.freedesktop.systemd1', '/org/freedesktop/systemd1', introspection)
        manager_interface = systemd_manager.get_interface('org.freedesktop.systemd1.Manager')

        # Start the service
        await manager_interface.call_start_unit(f'{service_name}.service', 'replace')

        log.info(f'Service {service_name} started successfully.')
        return True
    except Exception as e:
        log.error(f'Failed to start service {service_name}: {e}')
        return False

async def stop_service(service_name):
    try:
        # Connect to the system bus
        bus = await MessageBus(bus_type=BusType.SYSTEM).connect()

        # Get the introspection data
        introspection = await bus.introspect('org.freedesktop.systemd1', '/org/freedesktop/systemd1')

        # Get a proxy object for the systemd manager
        systemd_manager = bus.get_proxy_object('org.freedesktop.systemd1', '/org/freedesktop/systemd1', introspection)
        manager_interface = systemd_manager.get_interface('org.freedesktop.systemd1.Manager')

        # Stop the service
        await manager_interface.call_stop_unit(f'{service_name}.service', 'replace')

        log.info(f'Service {service_name} stopped successfully.')
        return True
    except Exception as e:
        log.error(f'Failed to stop service {service_name}: {e}')
        return False

def update_file(directory: Path, root_name: str):
    """
    Updates a file (e.g., versions.json) by renaming the new file to current and the current file to old.

    Parameters:
        directory (Path): Target directory where the file should be updated.
        root_name (str): Root name of the file (e.g., "versions.json").

    Returns:
        bool: True if successful, False otherwise.
    """

    current_file = directory / f"current_{root_name}"
    old_file = directory / f"old_{root_name}"
    new_file = directory / f"new_{root_name}"

    try:
        # If current_file exists, backup current_<root_name> by renaming it to old_<root_name>
        if current_file.exists():
            current_file.rename(old_file)
            log.info(f"Renamed {current_file} to {old_file}")
        else:
            log.info(f"{current_file} does not exist. Skipping backup.")

        # Rename new_<root_name> to current_<root_name>
        new_file.rename(current_file)
        log.info(f"Renamed {new_file} to {current_file}")

        return True

    except Exception as e:
        log.error(f"An error occurred during file update: {e}")
        rollback_file(current_file, old_file)
        return False

def rollback_file(current_file: Path, old_file: Path):
    """Restores the previous version of a file in case of failure."""
    try:
        # Check if the old_file exists
        if old_file.exists():
            # Remove the possibly corrupted current_file if it exists
            if current_file.exists():
                current_file.unlink()  # This removes the current file
                log.info(f"Removed corrupted {current_file}")

            # Restore the old file by renaming it back
            old_file.rename(current_file)
            log.info(f"Rolled back to previous {current_file} from {old_file}")
        else:
            log.error(f"Rollback failed: {old_file} does not exist.")
    except Exception as e:
        log.error(f"Failed to rollback {current_file}: {e}")

def rollback_all():
    def get_file_names(directory, root_name):
            current_file = directory / f"current_{root_name}"
            old_file = directory / f"old_{root_name}"
            return current_file, old_file
        # Update the docker-compose file

    current_file, old_file = get_file_names(UPDATE_DIR, COMPOSE_ROOT)
    rollback_file(current_file, old_file)

    current_file, old_file = get_file_names(UPDATE_DIR, VERSIONS_ROOT)
    rollback_file(current_file, old_file)


    current_file, old_file = get_file_names(UPDATE_DIR, FIRMWARE_ROOT)
    rollback_file(current_file, old_file)

    current_file, old_file = get_file_names(UPDATE_DIR, FW_JSON_ROOT)
    rollback_file(current_file, old_file)

    current_file, old_file = get_file_names(UPDATE_DIR, LED_FW_ROOT)
    rollback_file(current_file, old_file)

    current_file, old_file = get_file_names(UPDATE_DIR, LED_JSON_ROOT)
    rollback_file(current_file, old_file) 

# Trigger the update process asynchronously
async def trigger_update_process():
    try:
        # Check if images are different
        if not check_if_images_are_different():
            log.info("Unable to verify images are different. Stopping Update")
            send_update_status(BACKEND_URL, UpdateStatusEnum.NOT_STARTED)
            return

        response = {}

        # Read the new versions data
        new_versions_data = read_json_file(UPDATE_DIR / f"new_{VERSIONS_ROOT}")

        # Get the frontend and backend image names
        fe_image, be_image, as_image = get_image_names(new_versions_data)

        download_done = False
        try:
            fe_image = client.images.get(fe_image).tags[0]
            be_image = client.images.get(be_image).tags[0]
            as_image = client.images.get(as_image).tags[0]
            download_done = True
        except docker.errors.ImageNotFound:
            log.info(f"Images {fe_image} or {be_image} or {as_image} not found locally. Attempting to download.")
        except docker.errors.APIError as e:
            log.error(f"API error while checking images: {e}")
        except Exception as e:
            log.error(f"Error downloading images: {e}")

        while response.get("status") != 200:
            # Pull the Docker images
            while not download_done:
                if docker_login():
                    log.info("Successfully logged in to the Docker registry.")
                    if (pull_docker_image(fe_image) and pull_docker_image(be_image) and pull_docker_image(as_image)):
                        log.info(f"Successfully pulled images: {fe_image}, {be_image}, {as_image}")
                        download_done = True
                    else:
                        log.warning("Failed to pull images. Retrying...")
                        await asyncio.sleep(10)
                else:
                    log.warning("Failed to log in to the Docker registry. Retrying...")
                    await asyncio.sleep(10)

            response = send_update_ready(BACKEND_URL, True)
            resp_msg = response.get("response")
            delay = resp_msg.get("wait_to_update", 0) if resp_msg else 0

            # If the response message contains "do_update" as False, exit the function
            if resp_msg and (not resp_msg.get("do_update")) and (delay == 0):
                log.info("Backend says don't do update and don't wait for retry")
                return

            if resp_msg and delay > 0:
                await asyncio.sleep(delay)
                log.info(f"Waiting for {delay} seconds before retrying")

        # backend is about to die anyway....
        response = send_update_status(BACKEND_URL, UpdateStatusEnum.IN_PROGRESS)
        await asyncio.sleep(5) # wait a bit to show the update screen before blanking

        # Stop the compose_run service
        if not await stop_service("compose_run"):
            raise Exception("Failed to stop the compose_run service.")

        # Delete old .old file container images
        try:
            old_versions_data = read_json_file(UPDATE_DIR / f"old_{VERSIONS_ROOT}")
            old_fe_image, old_be_image, old_asset_image = get_image_names(old_versions_data)
            log.info(f"Attempting to remove old images: {old_fe_image}, {old_be_image}, {old_asset_image}")

            # Remove the old images if they exist and are not the same as the current images
            if fe_image != old_fe_image:
                try:
                    client.images.remove(old_fe_image)

                except docker.errors.ImageNotFound:
                    log.warning(f"Image {old_fe_image} already deleted?")
                except Exception as e:
                    log.error(f"An error occurred while removing image {old_fe_image}: {e}")

            if be_image != old_be_image:
                try:
                    client.images.remove(old_be_image)

                except docker.errors.ImageNotFound:
                    log.warning(f"Image {old_be_image} already deleted?")
                except Exception as e:
                    log.error(f"An error occurred while removing image {old_be_image}: {e}")

            if as_image != old_asset_image:
                try:
                    client.images.remove(old_asset_image)

                except docker.errors.ImageNotFound:
                    log.warning(f"Image {old_asset_image} already deleted?")
                except Exception as e:
                    log.error(f"An error occurred while removing image {old_asset_image}: {e}")

        except FileNotFoundError:
            log.warning("Old versions file not found. Skipping image deletion.")

        except Exception as e:
            log.error(f"Failed to delete old container images: {e}")

        # Update the docker-compose file
        if not update_file(UPDATE_DIR, COMPOSE_ROOT):
            raise Exception("Failed to update docker-compose file.")

        # Update the versions file
        if not update_file(UPDATE_DIR, VERSIONS_ROOT):
            raise Exception("Failed to update versions file.")

        # Update Firmware bundle
        if not update_file(UPDATE_DIR, FIRMWARE_ROOT):
            raise Exception("Failed to update firmware bundle.")

        # Update Firmware JSON
        if not update_file(UPDATE_DIR, FW_JSON_ROOT):
            raise Exception("Failed to update firmware JSON.")

        # Update LED Firmware
        if not update_file(UPDATE_DIR, LED_FW_ROOT):
            raise Exception("Failed to update LED firmware.")

        # Update LED Firmware JSON
        if not update_file(UPDATE_DIR, LED_JSON_ROOT):
            raise Exception("Failed to update LED firmware JSON.")

        # Start the compose_run service
        if not await start_service("compose_run"):
            raise Exception("Failed to start the compose_run service.")

        # Update successful
        service_start_time = time.time()
        response = {}
        while response.get('status') != 200:
            await asyncio.sleep(5)
            response = send_update_status(BACKEND_URL, UpdateStatusEnum.COMPLETED)
            if time.time() - service_start_time > 30:
                raise Exception("New backend didnt respond for 30 seconds")

        log.info(f"Update successful: {response}")

    except Exception as e:
        # If any failure, start the compose_run service with the old configuration
        rollback_all()
        if not await start_service("compose_run"):
            log.error("Failed to start the compose_run service.")
        log.error(f"Error during startup: {e}")
        response = {}
        old_service_start_time = time.time()
        while response.get('status') != 200:
            response = send_update_status(BACKEND_URL, UpdateStatusEnum.FAILED)
            await asyncio.sleep(3)
            if time.time() - old_service_start_time > 30:
                log.error("Old backend didnt respond for 30 seconds - this is bad")

                break


        log.info(f"Update failed: {response}")

async def start_journal_process():
    global journal_process
    # Build the journalctl command based on logged_modules
    command = ['journalctl', '-f', '-o', 'cat']
    if logged_modules and "all" not in logged_modules:
        for module in logged_modules:
            command.extend(['-u', module])
    journal_process = await asyncio.create_subprocess_exec(
        *command, stdout=asyncio.subprocess.PIPE, stderr=asyncio.subprocess.STDOUT
    )
    log.info(f"Started journalctl with command: {' '.join(command)}")

async def stop_journal_process():
    global journal_process, journal_reader_task
    if journal_process:
        journal_process.terminate()
        await journal_process.wait()
        journal_process = None
        log.info("Stopped journalctl process.")
    # Cancel the journal_reader task
    if journal_reader_task and not journal_reader_task.done():
        journal_reader_task.cancel()
        log.info("Cancelled journal_reader task.")

async def journal_reader():
    try:
        while True:
            if journal_process is None:
                await asyncio.sleep(0.1)
                continue
            raw_line = await journal_process.stdout.readline()
            if not raw_line:
                await asyncio.sleep(0.1)
                continue
            line = raw_line.decode('utf-8', errors='replace')
            try:
                await log_queue.put(line)
            except asyncio.QueueFull:
                log.warning("Log queue is full. Dropping log message.")
    except asyncio.CancelledError:
        log.info("Journal reader task cancelled.")
    except Exception as e:
        log.error(f"Error in journal_reader: {e}")


async def execute_update_commands(update_script_name):
    update_script_path = UPDATE_DIR / f'copy/{update_script_name}'
    command = [str(update_script_path)]
    try:
        log.info(f"Started update script with command: {' '.join(command)}")

        # Execute the command
        result = subprocess.run(command, shell=True, text=True, capture_output=True, cwd=str(UPDATE_DIR / 'copy'))

        output = result.stdout
        error = result.stderr

        if result.returncode == 0:
            log.info(f"Update script executed successfully: {output}")
        else:
            log.error(f"Update script failed: {error}")

        return output, error
    except Exception as e:
        log.error(f"Error executing update script: {e}")
        return None, str(e)

@app.post("/execute_system_update")
async def execute_system_update(update_request: SystemUpdateRequest):
    log.info(f"Executing system update using script: {update_request.update_script_name}")
    asyncio.create_task(execute_update_commands(update_request.update_script_name))
    send_update_status(BACKEND_URL, UpdateStatusEnum.IN_PROGRESS) # for now we just say we are updatings, dont wait for confirmation from BE
    return "Update process started."
    
# POST endpoint that triggers the update
@app.post("/trigger_update")
async def trigger_update(request: UpdateRequest):
    if request.update_ready:
        log.info("Received request to trigger update process.")

        # Start the update process in the background
        asyncio.create_task(trigger_update_process(), name="update_process_task")
        response = {"status": 200, "response": "Update process started."}
        return response
    else:
        return HTTPException(status_code=400, detail="Invalid request to trigger update process.")

@app.post("/enable_logging")
async def enable_logging(data: dict):
    global journal_process, logged_modules, journal_reader_task
    command = data.get("command")
    if command == "start":
        modules = data.get("modules", [])
        logged_modules = modules
        # Start the journalctl process
        if journal_process is None or journal_process.returncode is not None:
            await start_journal_process()
            # Start the journal_reader task
            if journal_reader_task is None or journal_reader_task.done():
                journal_reader_task = asyncio.create_task(journal_reader(), name="journal_reader_task")
        return {"status": "logging started"}
    elif command == "stop":
        # Stop the journalctl process
        await stop_journal_process()
        return {"status": "logging stopped"}
    else:
        return {"status": "unknown command"}, 400

@app.websocket("/ws/logs")
async def websocket_logs(websocket: WebSocket):
    global connected_websockets
    await websocket.accept()
    connected_websockets.append(websocket)
    log.info("WebSocket connection accepted for streaming logs.")
    try:
        while True:
            data = await log_queue.get()
            await websocket.send_text(data)
    except WebSocketDisconnect:
        connected_websockets.remove(websocket)
        log.info("WebSocket connection closed.")
        if not connected_websockets:
            await stop_journal_process()
    except Exception as e:
        log.error(f"Exception in websocket_logs: {e}")
        connected_websockets.remove(websocket)
        if not connected_websockets:
            await stop_journal_process()
            