import { IPayrollDetail } from './iPayrollDetail';

export interface IPayrollSummary {
    employeeId: number;
    yearMonth: string;
    basicPay: number;
    overtimePay: number;
    deductions: number;
    netPay: number;
    details: IPayrollDetail[];
}
