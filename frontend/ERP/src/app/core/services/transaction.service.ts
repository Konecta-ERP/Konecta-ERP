import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { baseURL } from '../apiRoot/baseURL';
import { iCreateJournalTransaction } from '../interfaces/iTransaction';

@Injectable({
    providedIn: 'root',
})
export class TransactionService {
    private controllerUrl = `${baseURL}/finance/journal-transactions`;

    constructor(private _httpClient: HttpClient) {}

    createTransaction(data: iCreateJournalTransaction): Observable<any> {
        return this._httpClient.post(this.controllerUrl, data);
    }

    getAllTransactions(): Observable<any> {
        return this._httpClient.get(this.controllerUrl);
    }
}
