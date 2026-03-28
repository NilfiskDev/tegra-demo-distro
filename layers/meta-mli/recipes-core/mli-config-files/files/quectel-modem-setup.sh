#!/bin/sh
# Switch Quectel EM05-G modem from ECM/RNDIS mode to QMI mode
# This only needs to happen once as the setting is persistent in the modem firmware.
#
# USB net modes:
#   0 = QMI (RMNET) - required for ModemManager/libqmi
#   1 = ECM
#   2 = MBIM
#   3 = RNDIS

MODEM_PORT=""
RETRY=0
MAX_RETRY=30

# Wait for the modem AT port to appear
while [ -z "$MODEM_PORT" ] && [ $RETRY -lt $MAX_RETRY ]; do
    for port in /dev/ttyUSB*; do
        if [ -c "$port" ]; then
            # Try sending AT to see if this port responds
            RESPONSE=$(echo -e "AT\r" | timeout 2 cat > "$port" 2>/dev/null && timeout 2 cat < "$port" 2>/dev/null || true)
            if echo "$RESPONSE" | grep -q "OK"; then
                MODEM_PORT="$port"
                break
            fi
        fi
    done
    if [ -z "$MODEM_PORT" ]; then
        RETRY=$((RETRY + 1))
        sleep 1
    fi
done

if [ -z "$MODEM_PORT" ]; then
    echo "quectel-setup: No modem AT port found after ${MAX_RETRY}s, trying first available ttyUSB"
    for port in /dev/ttyUSB*; do
        if [ -c "$port" ]; then
            MODEM_PORT="$port"
            break
        fi
    done
fi

if [ -z "$MODEM_PORT" ]; then
    echo "quectel-setup: ERROR - No /dev/ttyUSB ports found. Cannot configure modem."
    exit 1
fi

echo "quectel-setup: Using AT port $MODEM_PORT"

# Configure the serial port
stty -F "$MODEM_PORT" 115200 raw -echo

# Check current USB net mode
echo -e "AT+QCFG=\"usbnet\"\r" > "$MODEM_PORT"
sleep 1
CURRENT_MODE=$(timeout 3 cat < "$MODEM_PORT" 2>/dev/null | grep "+QCFG:" | grep -o '[0-9]*$' || true)

echo "quectel-setup: Current USB net mode: ${CURRENT_MODE:-unknown}"

if [ "$CURRENT_MODE" = "0" ]; then
    echo "quectel-setup: Modem already in QMI mode, no change needed."
    exit 0
fi

echo "quectel-setup: Switching modem to QMI mode (0)..."
echo -e "AT+QCFG=\"usbnet\",0\r" > "$MODEM_PORT"
sleep 2

# Reset the modem to apply the change
echo "quectel-setup: Resetting modem to apply USB mode change..."
echo -e "AT+CFUN=1,1\r" > "$MODEM_PORT"
sleep 5

echo "quectel-setup: Modem has been switched to QMI mode. It will re-enumerate on USB."
