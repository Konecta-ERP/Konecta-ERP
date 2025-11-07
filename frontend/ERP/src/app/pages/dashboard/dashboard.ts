import { Component } from '@angular/core';
import { Card } from "../../components/card/card";

interface CardConfig {
  title: string;
  subtitle: string;
  imageUrl: string;
  route?: string;
}

@Component({
  selector: 'app-dashboard',
  imports: [Card],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {
  userRole: 'employee' | 'manager' | 'admin' = 'employee'; // This could come from a service
  cards: CardConfig[] = [];

  ngOnInit() {
    this.cards = this.getCardsForRole(this.userRole);
  }

  private getCardsForRole(role: string): CardConfig[] {
    const cardConfigs: Record<string, CardConfig[]> = {
      employee: [
        {
          title: 'Leave Request',
          subtitle: 'Submit and track your leave requests',
          imageUrl: 'https://primefaces.org/cdn/primeng/images/card-ng.jpg',
          route: '/leave-request'
        },
        {
          title: 'View Profile',
          subtitle: 'View and edit your profile information',
          imageUrl: 'https://primefaces.org/cdn/primeng/images/card-ng.jpg',
          route: '/profile'
        },
        {
          title: 'Timesheet',
          subtitle: 'Log your working hours',
          imageUrl: 'https://primefaces.org/cdn/primeng/images/card-ng.jpg',
          route: '/timesheet'
        }
      ],
      manager: [
        {
          title: 'Leave Request',
          subtitle: 'Submit and track your leave requests',
          imageUrl: 'https://primefaces.org/cdn/primeng/images/card-ng.jpg',
          route: '/leave-request'
        },
        {
          title: 'View Profile',
          subtitle: 'View and edit your profile information',
          imageUrl: 'https://primefaces.org/cdn/primeng/images/card-ng.jpg',
          route: '/profile'
        },
        {
          title: 'Approve Leaves',
          subtitle: 'Review and approve team leave requests',
          imageUrl: 'https://primefaces.org/cdn/primeng/images/card-ng.jpg',
          route: '/approve-leaves'
        },
        {
          title: 'Team Management',
          subtitle: 'Manage your team members',
          imageUrl: 'https://primefaces.org/cdn/primeng/images/card-ng.jpg',
          route: '/team'
        },
        {
          title: 'Reports',
          subtitle: 'View team performance reports',
          imageUrl: 'https://primefaces.org/cdn/primeng/images/card-ng.jpg',
          route: '/reports'
        }
      ],
      admin: [
        {
          title: 'User Management',
          subtitle: 'Manage all system users',
          imageUrl: 'https://primefaces.org/cdn/primeng/images/card-ng.jpg',
          route: '/users'
        },
        {
          title: 'System Settings',
          subtitle: 'Configure system preferences',
          imageUrl: 'https://primefaces.org/cdn/primeng/images/card-ng.jpg',
          route: '/settings'
        },
        {
          title: 'All Reports',
          subtitle: 'Access all organizational reports',
          imageUrl: 'https://primefaces.org/cdn/primeng/images/card-ng.jpg',
          route: '/all-reports'
        }
      ]
    };

    return cardConfigs[role] || [];
  }
}
