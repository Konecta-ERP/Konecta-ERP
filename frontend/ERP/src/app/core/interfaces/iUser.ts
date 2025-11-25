export interface User {
    id: string;
    employeeId?: number;
    firstName: string;
    lastName: string;
    email: string;
    role: string;
    position?:string
    profilePictureUrl?: string;
    phone?: string;
    createdAt?: Date;
    departmentId?: number;
    departmentName?: string;
    salaryNet?: number;
    salaryGross?: number;
}
