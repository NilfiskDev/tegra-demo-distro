#!/bin/bash
# Script to setup CAN transceiver.

### This script may be invoked via a systemd service as follows
#
#	[Unit]
#	Description=Nilfisk SocketCAN dependencies
#	After=network.target
#
#	[Service]
#	ExecStart=/home/jerry/Tools/setup_can0.sh
#	RemainAfterExit=yes
#	User=root
#	Group=root
#	Type=oneshot
#
#	[Install]
#	WantedBy=multi-user.target
#

# ************************************************************************
#
# Device may be CAN0, CAN1 ... ?
#
# ************************************************************************
 
CAN_INSTANCE=can0
 
echo -e "\nJ17 sourced CAN status:"
cat /proc/device-tree/mttcan@c310000/status
echo -e "\n"
echo "J30 sourced CAN status:"
cat /proc/device-tree/mttcan@c320000/status
echo -e "\n"

echo -e "1st. driver."
if ! lsmod | grep 'mttcan' > /dev/null; then
    echo "mttcan NOT loaded, loading..."
    modprobe mttcan
else
    echo "mttcan driver already loaded."  
fi

echo -e "\n2nd. driver."
if ! lsmod | grep 'can_dev'  > /dev/null; then
    echo "can_dev NOT loaded, loading..."
    modprobe can_dev
else
    echo "can_dev driver already loaded."      
fi
echo ""

echo "Din pin-mux value:"
devmem2 0x0c303018
echo "Dout pin-mux value:"
devmem2 0x0c303010
echo ""

echo "Updating Din & Dout pin-muxes..."
devmem2 0x0c303018 w 0xc458
devmem2 0x0c303010 w 0xc400
echo ""

echo "Din pin-mux value:"
devmem2 0x0c303018
echo "Dout pin-mux value:"
devmem2 0x0c303010
echo ""

echo "Configuring CAN I/F for 250Kbaud."
ip link set down ${CAN_INSTANCE} 
ip link set ${CAN_INSTANCE} type can sjw 4 dsjw 4 berr-reporting on bitrate 250000
ip link set up ${CAN_INSTANCE}

ip -details -statistics link show ${CAN_INSTANCE}
