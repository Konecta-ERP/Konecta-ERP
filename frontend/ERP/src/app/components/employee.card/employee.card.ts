import { Component,Input,Output,EventEmitter } from '@angular/core';
import { SharedModule } from '../../shared/module/shared/shared-module';
import { iEmployeeSearchResponse } from '../../core/interfaces/iEmployeeSearchResponse';
import { Router } from '@angular/router';
@Component({
  selector: 'app-employee.card',
  imports: [SharedModule],
  templateUrl: './employee.card.html',
  styleUrl: './employee.card.css',
})
export class EmployeeCard {
    @Input({ required: true }) employee!: iEmployeeSearchResponse;
    @Output() cardClick = new EventEmitter<iEmployeeSearchResponse>();

    constructor(private router: Router) {}

    onCardClick(): void {
        // Navigate to employee detail page
        // this.router.navigate(['/employees', this.employee.id]);
        this.cardClick.emit(this.employee);
    }

    onViewProfile(event: Event): void {
        event.stopPropagation(); // Prevent card click
        // Navigate to employee profile
        // this.router.navigate(['/employees', this.employee.id, 'profile']);
    }
}
