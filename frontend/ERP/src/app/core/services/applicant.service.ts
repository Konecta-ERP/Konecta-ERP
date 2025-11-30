import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { baseURL } from '../apiRoot/baseURL';

export interface ApplicantDto {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    cvFileName: string;
    cvFileType: string;
    cvFilePath: string;
    coverLetter: string;
    status: string;
    appliedAt: string;
    postId: number;
}

export interface ApplicantApplicationDto {
    firstName: string;
    lastName: string;
    email: string;
    coverLetter: string;
    postId: number;
}

export interface UpdateApplicantStatusDto {
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
export class ApplicantService {
    private apiBaseUrl = `${baseURL}/applicants`;
    private jobPostsApiUrl = `${baseURL}/job-posts`;

    constructor(private http: HttpClient) {}

    /**
     * Submit job application with CV file
     */
    submitApplication(
        postId: number,
        dto: ApplicantApplicationDto,
        file: File
    ): Observable<ApiResponse<ApplicantDto>> {
        const formData = new FormData();
        const blob = new Blob([JSON.stringify(dto)], { type: 'application/json' });
        formData.append('data', blob);
        formData.append('file', file);

        return this.http.post<ApiResponse<ApplicantDto>>(
            `${this.jobPostsApiUrl}/${postId}/apply`,
            formData
        );
    }

    /**
     * Get applicant by ID
     */
    getApplicant(id: number): Observable<ApiResponse<ApplicantDto>> {
        return this.http.get<ApiResponse<ApplicantDto>>(`${this.apiBaseUrl}/${id}`);
    }

    /**
     * Get applicant by email
     */
    getApplicantByEmail(email: string): Observable<ApiResponse<ApplicantDto>> {
        return this.http.get<ApiResponse<ApplicantDto>>(`${this.apiBaseUrl}/email/${email}`);
    }

    /**
     * Get all applicants for a job post
     */
    getApplicantsForPost(postId: number): Observable<ApiResponse<ApplicantDto[]>> {
        return this.http.get<ApiResponse<ApplicantDto[]>>(
            `${this.jobPostsApiUrl}/${postId}/applicants`
        );
    }

    /**
     * Update applicant status
     */
    updateApplicantStatus(
        id: number,
        dto: UpdateApplicantStatusDto
    ): Observable<ApiResponse<ApplicantDto>> {
        return this.http.patch<ApiResponse<ApplicantDto>>(`${this.apiBaseUrl}/${id}/status`, dto);
    }

    deleteApplicant(applicantId: number): Observable<any> {
        return this.http.delete(`${this.apiBaseUrl}/${applicantId}`);
    }

    /**
     * Download CV for an applicant
     */
    downloadCv(applicantId: number): Observable<Blob> {
        return this.http.get(`${this.apiBaseUrl}/${applicantId}/download-cv`, {
            responseType: 'blob',
        });
    }
}
