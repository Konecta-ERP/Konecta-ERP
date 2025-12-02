import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { baseURL } from '../apiRoot/baseURL';

export interface LineItem {
    description: string;
    quantity: number;
    unit_price: number;
    line_total: number;
}

export interface InvoiceData {
    supplier_name: string;
    invoice_number: string;
    invoice_date: string;
    total_amount: number;
    currency: string;
    line_items: LineItem[];
    field_confidences: any;
}

export interface ValidationResult {
    status: string;
    errors: string[];
}

export interface ScanResponse {
    extracted_data: InvoiceData;
    validation: ValidationResult;
}

@Injectable({
    providedIn: 'root',
})
export class DocumentService {
    private apiUrl = `${baseURL}/documents/process-invoice`;

    constructor(private http: HttpClient) {}

    scanInvoice(file: File): Observable<ScanResponse> {
        const formData = new FormData();
        formData.append('file', file);
        return this.http.post<ScanResponse>(`${this.apiUrl}?save_file=false`, formData);
    }
}
