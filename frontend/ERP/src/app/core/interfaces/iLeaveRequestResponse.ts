export interface ILeaveRequestResponse {
    id: string;
    employeeId: string;
    startDate: Date;
    endDate: Date;
    reason: string;
    requestType: string;
    status: string;
}
