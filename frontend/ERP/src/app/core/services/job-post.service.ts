import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { baseURL } from '../apiRoot/baseURL';

export interface JobRequirement {
    text: string;
    mandatory: boolean;
}

export interface CreateJobPostDto {
    title: string;
    description: string;
    requirements: JobRequirement[];
    requisitionId: number;
}

export interface JobPostDto {
    id: number;
    title: string;
    description: string;
    requirements: JobRequirement[];
    active: boolean;
    postedAt: string;
    requisitionId: number;
}
export interface ApplicantApplicationDto {
    firstName: string;
    lastName: string;
    email: string;
    coverLetter: string;
    cvUrl: string;
    postId: number;
}

export interface ApplicantDto {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    cvUrl: string;
    coverLetter: string;
    status: string;
    appliedAt: string;
    postId: number;
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
export class JobPostService {
    private apiBaseUrl = `${baseURL}/job-posts`;

    constructor(private http: HttpClient) {}

    /**
     * Create a new job post
     */
    createJobPost(dto: CreateJobPostDto): Observable<ApiResponse<JobPostDto>> {
        return this.http.post<ApiResponse<JobPostDto>>(this.apiBaseUrl, dto);
    }

    /**
     * Get a specific job post by ID
     */
    getJobPost(id: number): Observable<ApiResponse<JobPostDto>> {
        return this.http.get<ApiResponse<JobPostDto>>(`${this.apiBaseUrl}/${id}`);
    }

    /**
     * Search job posts with optional filters
     */
    searchJobPosts(
        position?: string,
        departmentId?: number,
        active?: boolean
    ): Observable<ApiResponse<JobPostDto[]>> {
        let url = `${this.apiBaseUrl}/search`;
        const params = new URLSearchParams();

        if (position) {
            params.append('position', position);
        }
        if (departmentId) {
            params.append('departmentId', departmentId.toString());
        }
        if (active !== undefined) {
            params.append('active', active.toString());
        }

        if (params.toString()) {
            url += `?${params.toString()}`;
        }

        return this.http.get<ApiResponse<JobPostDto[]>>(url);
    }

    /**
     * Set job post active status
     */
    setJobPostActive(id: number, active: boolean): Observable<ApiResponse<JobPostDto>> {
        return this.http.patch<ApiResponse<JobPostDto>>(`${this.apiBaseUrl}/${id}/active`, {
            active,
        });
    }

    /**
     * Submit job application
     */
    submitApplication(
        id: number,
        dto: ApplicantApplicationDto
    ): Observable<ApiResponse<ApplicantDto>> {
        return this.http.post<ApiResponse<ApplicantDto>>(`${this.apiBaseUrl}/${id}/apply`, dto);
    }

    /**
     * Get applicants for a job post
     */
    getApplicantsForPost(postId: number): Observable<ApiResponse<any[]>> {
        return this.http.get<ApiResponse<any[]>>(`${this.apiBaseUrl}/${postId}/applicants`);
    }
}
