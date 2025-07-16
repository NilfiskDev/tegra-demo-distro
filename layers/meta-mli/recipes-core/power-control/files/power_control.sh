#!/bin/bash

# Path to the monitored file
FILE_PATH="/usr/config/power"

# Check if the file exists
if [ ! -f "$FILE_PATH" ]; then
    echo "File $FILE_PATH does not exist."
    exit 1
fi

# Function to check file content and execute command
check_and_execute() {
    local content=$(cat "$FILE_PATH")

    if [ "$content" == "suspend" ]; then
        systemctl suspend
        # Clear the file content after executing the command
        > "$FILE_PATH"
    elif [ "$content" == "reboot" ]; then
        systemctl reboot
        # Clear the file content after executing the command
        > "$FILE_PATH"
    elif [ "$content" == "shutdown" ]; then
        systemctl poweroff
        # Clear the file content after executing the command
        > "$FILE_PATH"
    elif ! [ -z "$content" ]; then
        echo "Invalid command: $content"
    fi
}

# Main loop
while true; do
    check_and_execute
    # Wait for 100 milliseconds
    sleep 0.1
done
