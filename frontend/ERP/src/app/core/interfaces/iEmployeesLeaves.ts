import { ILeaveRequestResponse } from "./iLeaveRequestResponse";
import { User } from "./iUser";

export interface IEmployeesLeaves {
    employee: User;
    leaveRequests: ILeaveRequestResponse[];
}
