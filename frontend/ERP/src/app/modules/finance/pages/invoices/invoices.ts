import { Component, ViewChild } from '@angular/core';
import {
    DocumentService,
    InvoiceData,
    ValidationResult,
} from '../../../../core/services/document.service';

import { SharedModule } from '../../../../shared/module/shared/shared-module';
import { MessageService } from 'primeng/api';
// PrimeNG Imports
import { FileUploadModule, FileUploadHandlerEvent, FileUpload } from 'primeng/fileupload';
import { ProgressBarModule } from 'primeng/progressbar';

@Component({
    selector: 'app-invoices',
    standalone: true,
    imports: [SharedModule, FileUploadModule, ProgressBarModule],
    providers: [MessageService],
    templateUrl: './invoices.html',
    styleUrls: ['./invoices.css'],
})
export class Invoices {
    // 1. Get reference to the HTML element
    @ViewChild('fileUploader') fileUploader!: FileUpload;

    invoice: InvoiceData | null = null;
    validation: ValidationResult | null = null;
    isLoading = false;

    constructor(private docService: DocumentService, private messageService: MessageService) {}

    onUpload(event: FileUploadHandlerEvent) {
        const file = event.files[0];
        if (!file) return;

        this.isLoading = true;
        this.invoice = null;

        this.docService.scanInvoice(file).subscribe({
            next: (response) => {
                this.invoice = response.extracted_data;
                this.validation = response.validation;
                this.isLoading = false;

                // 2. FIX: Remove the file from the list (removing the "Pending" label)
                this.fileUploader.clear();

                this.messageService.add({
                    severity: 'success',
                    summary: 'Scan Complete',
                    detail: `Processed invoice from ${
                        this.invoice.supplier_name || 'Unknown Vendor'
                    }`,
                });
            },
            error: (err) => {
                console.error(err);
                this.isLoading = false;
                // Don't clear on error so user can retry if they want
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'Failed to process document.',
                });
            },
        });
    }

    // Helper to get severity for validation status
    getStatusSeverity(
        status: string
    ): 'success' | 'warn' | 'danger' | 'info' | 'secondary' | 'contrast' | undefined {
        return status === 'VALID' ? 'success' : 'warn';
    }
}
