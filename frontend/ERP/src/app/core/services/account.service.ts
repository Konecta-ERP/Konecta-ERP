import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { baseURL } from '../../core/apiRoot/baseURL';
import { iCreateAccount, iUpdateAccount, iAccount } from '../interfaces/iAccount';

@Injectable({
    providedIn: 'root',
})
export class AccountService {
    private controllerUrl = `${baseURL}/finance/accounts`;

    constructor(private _httpClient: HttpClient) {}

    /**
     * Create a new GL Account
     * Endpoint: POST /api/finance/accounts
     * Body: CreateAccountDTO
     */
    createAccount(data: iCreateAccount): Observable<any> {
        console.log('Creating new account with data:', data);
        return this._httpClient.post(`${this.controllerUrl}`, data);
    }

    /**
     * Fetch all GL Accounts
     * Endpoint: GET /api/finance/accounts
     * Response: List<AccountDTO>
     */
    getAllAccounts(): Observable<any> {
        return this._httpClient.get<iAccount[]>(`${this.controllerUrl}`);
    }

    /**
     * Update an existing GL Account
     * Endpoint: PUT /api/finance/accounts/{id}
     * Body: UpdateAccountDTO
     */
    updateAccount(id: number, data: iUpdateAccount): Observable<any> {
        console.log('Updating account ID:', id, 'with data:', data);
        return this._httpClient.put(`${this.controllerUrl}/${id}`, data);
    }

    /**
     * Deactivate a GL Account (Soft Delete)
     * Endpoint: PUT /api/finance/accounts/deactivate/{id}
     */
    deactivateAccount(id: number): Observable<any> {
        console.log('Deactivating account ID:', id);
        return this._httpClient.put(`${this.controllerUrl}/deactivate/${id}`, {});
    }
}
