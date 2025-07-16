from pydantic import BaseModel
from enum import IntEnum

class UpdateRequest(BaseModel):
    update_ready: bool

class UpdateStatusEnum(IntEnum):
    NOT_STARTED = 0
    IN_PROGRESS = 1
    COMPLETED = 2
    FAILED = 3

class UpdateStatus(BaseModel):
    update_status: UpdateStatusEnum
    