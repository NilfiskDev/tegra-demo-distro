import evdev

SCREEN_NAME = "wch.cn ChipOne Hid DFU Tool"

def process_event(event, uinput):
    if event.type == evdev.ecodes.EV_ABS and event.code == evdev.ecodes.ABS_MT_SLOT:
        # ignore slot events
        return
    uinput.write_event(event) # push everything else through to the virtual input  


try:
    devices = [evdev.InputDevice(dev) for dev in evdev.list_devices()]

    device = None
    for dev in devices:
        if dev.name == SCREEN_NAME:
            device = dev

    if device is None:
        print("No device found with name: " + SCREEN_NAME)
        exit(1)

    # mirror the input
    uinput = evdev.UInput.from_device(device, name="multitouch_disabled")

    for event in device.read_loop():
        # this runs forever
        process_event(event, uinput)

except Exception as e:
    print(e)
    print("Something went wrong, exiting")
    exit(2)
