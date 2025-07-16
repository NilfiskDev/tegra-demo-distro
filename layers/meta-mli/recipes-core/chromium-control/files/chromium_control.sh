#!/bin/bash

# Path to the monitored file
FILE_PATH="/usr/config/chromium"

# Ensure the file exists with correct permissions
if [ ! -f "$FILE_PATH" ]; then
    echo "File $FILE_PATH does not exist. Creating it."
    touch "$FILE_PATH"
    chmod 700 "$FILE_PATH"
fi

# Global variable to store the PID of the startx process
STARTX_PID=""

check_and_execute() {
    local content
    content=$(cat "$FILE_PATH")

    if [ "$content" == "start" ]; then
        # Start a new X session and store its PID
        echo "Starting new X session."
        startx -- -nocursor -depth 24 -dpi 170 &
        # Clear the file content
        > "$FILE_PATH"

    elif [ "$content" == "exit" ]; then
        echo "Exiting X session"
        # Kill the X session
        pkill -P $(pgrep startx) --signal SIGTERM

        while pgrep -f xinit > /dev/null; do
            sleep 0.1
        done
        
        # Clear the file content
        > "$FILE_PATH"

    elif [ -n "$content" ]; then
        echo "Invalid command: $content"
        > "$FILE_PATH"
    fi
}

# Monitor the file for changes using inotifywait
while true; do
    check_and_execute
    sleep 0.1
done
