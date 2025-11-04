import { Component } from '@angular/core';
import { SharedModule } from '../../shared/module/shared/shared-module';
import { EmployeeGoals } from '../../components/employee-goals/employee-goals';
import { EmployeeLeaveRequests } from '../../components/employee-leave-requests/employee-leave-requests';
import { EmployeePerformanceReviews } from '../../components/employee-performance-reviews/employee-performance-reviews';
import { UserService } from '../../core/services/user.service';
import { User } from '../../core/interfaces/iUser';
import format from '../../shared/functions/shared-functions';
@Component({
  selector: 'app-profile',
  imports: [SharedModule, EmployeeGoals,EmployeeLeaveRequests,EmployeePerformanceReviews],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile {
    user:User | null;
    formattedHireDate: string = 'N/A';

    constructor(private _userService:UserService) {
        this.user = this._userService.getUser();
        if (this.user)
            {
            this.formattedHireDate = format(this.user?.createdAt, 'date');
        }
    }



    activeTab = 'Leave Requests';

  setActive(tab: string) {
    this.activeTab = tab;
  }
}


// name , email, position, department, profilePictureUrl, salary, hireDate
