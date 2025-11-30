import { Component,Input,Output,EventEmitter } from '@angular/core';
import { SharedModule } from '../../shared/module/shared/shared-module';
import { iEmployeeSearchResponse } from '../../core/interfaces/iEmployeeSearchResponse';
import { Router } from '@angular/router';
import { User } from '../../core/interfaces/iUser';
@Component({
  selector: 'app-employee-card',
  imports: [SharedModule],
  templateUrl: './employee.card.html',
  styleUrl: './employee.card.css',
})
export class EmployeeCard {
    @Input({ required: true }) employee!: User;
    @Output() cardClick = new EventEmitter<User>();

    constructor(private router: Router) {}

    onCardClick(): void {
        this.router.navigate(['/home/profile', this.employee.employeeId]);
    }

    onViewProfile(event: Event): void {
        event.stopPropagation();
    }
}
