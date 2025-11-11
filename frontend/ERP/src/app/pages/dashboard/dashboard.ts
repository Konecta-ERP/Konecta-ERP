import { Component } from '@angular/core';
import { SharedModule } from '../../shared/module/shared/shared-module';
import { EmployeeGoals } from '../../components/employee-goals/employee-goals';
import { EmployeeLeaveRequests } from '../../components/employee-leave-requests/employee-leave-requests';
import { EmployeePerformanceReviews } from '../../components/employee-performance-reviews/employee-performance-reviews';
interface CardConfig {
  title: string;
  subtitle: string;
  imageUrl: string;
  route?: string;
}

@Component({
  selector: 'app-dashboard',
  imports: [SharedModule, EmployeeGoals,EmployeeLeaveRequests,EmployeePerformanceReviews],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {

    activeTab = 'Career Goals';

    setActive(tab: string) {
        this.activeTab = tab;
    }

}
