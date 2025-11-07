export interface ILeaveRequestRequest {
    startDate: Date;
    endDate: Date;
    reason: string;
    requestType: 'ANNUAL' | 'SICK' | 'UNPAID' | 'OTHER';
}
