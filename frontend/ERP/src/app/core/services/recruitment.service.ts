import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { baseURL } from '../apiRoot/baseURL';

export interface RequisitionStatus {
    value: string;
    label: string;
}

export interface RequisitionPriority {
    value: string;
    label: string;
}

export interface JobRequisitionDto {
    id: number;
    reason: string;
    priority: string;
    openings: number;
    status: string;
    createdAt: string;
    departmentId: number;
}

export interface CreateRequisitionDto {
    reason: string;
    priority: string;
    openings: number;
    departmentId: number;
}

export interface UpdateRequisitionDto {
    reason?: string;
    priority?: string;
    openings?: number;
    departmentId?: number;
}

export interface UpdateRequisitionStatusDto {
    status: string;
}

export interface ApiResponse<T> {
    status: number;
    message: string;
    cMessage: string;
    data: T;
}

@Injectable({
    providedIn: 'root',
})
export class RecruitmentService {
    private apiBaseUrl = `${baseURL}/job-requisitions`;

    constructor(private http: HttpClient) {}

    /**
     * Create a new job requisition
     */
    createRequisition(dto: CreateRequisitionDto): Observable<ApiResponse<JobRequisitionDto>> {
        return this.http.post<ApiResponse<JobRequisitionDto>>(this.apiBaseUrl, dto);
    }

    /**
     * Search job requisitions with optional filters
     */
    searchRequisitions(
        departmentId?: number,
        status?: string
    ): Observable<ApiResponse<JobRequisitionDto[]>> {
        let url = `${this.apiBaseUrl}/search`;
        const params = new URLSearchParams();

        if (departmentId) {
            params.append('departmentId', departmentId.toString());
        }
        if (status) {
            params.append('status', status);
        }

        if (params.toString()) {
            url += `?${params.toString()}`;
        }

        return this.http.get<ApiResponse<JobRequisitionDto[]>>(url);
    }

    /**
     * Get a specific job requisition by ID
     */
    getRequisition(id: number): Observable<ApiResponse<JobRequisitionDto>> {
        return this.http.get<ApiResponse<JobRequisitionDto>>(`${this.apiBaseUrl}/${id}`);
    }

    /**
     * Update a job requisition
     */
    updateRequisition(
        id: number,
        dto: UpdateRequisitionDto
    ): Observable<ApiResponse<JobRequisitionDto>> {
        return this.http.patch<ApiResponse<JobRequisitionDto>>(`${this.apiBaseUrl}/${id}`, dto);
    }

    /**
     * Update the status of a job requisition (requires HR_MANAGER role)
     */
    updateRequisitionStatus(
        id: number,
        dto: UpdateRequisitionStatusDto
    ): Observable<ApiResponse<JobRequisitionDto>> {
        return this.http.patch<ApiResponse<JobRequisitionDto>>(
            `${this.apiBaseUrl}/${id}/status`,
            dto
        );
    }

    /**
     * Delete a job requisition by ID
     */
    deleteRequisition(id: number): Observable<ApiResponse<null>> {
        return this.http.delete<ApiResponse<null>>(`${this.apiBaseUrl}/${id}`);
    }
}
