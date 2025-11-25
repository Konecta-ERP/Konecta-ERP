export interface ILeaveRequestRequest {
    startDate: Date;
    endDate: Date;
    reason: string;
    requestType: 'VACATION' | 'SICK' | 'UNPAID';
}
