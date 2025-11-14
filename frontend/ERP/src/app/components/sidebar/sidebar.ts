import { Component,Input } from '@angular/core';
import { SharedModule } from '../../shared/module/shared/shared-module';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

export interface SidebarItem {
  label: string;
  icon: string;
  route: string;
  roles?: string[];
}

@Component({
  selector: 'app-sidebar',
  imports: [SharedModule,RouterModule, CommonModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar {
    @Input() items: SidebarItem[] = [];
    @Input() basePath: string = '';
    @Input() userRole: string = '';

    get filteredItems() {
        return this.items.filter(i => !i.roles || i.roles.includes(this.userRole));
    }

    collapsed = false;
    currentYear = new Date().getFullYear();


    toggleCollapse() {
        this.collapsed = !this.collapsed;
    }
}
