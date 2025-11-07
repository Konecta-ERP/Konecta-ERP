import { Component } from '@angular/core';
import { SharedModule } from '../../shared/module/shared/shared-module';
import { MenuItem } from 'primeng/api';
import { RouterLink } from "@angular/router";
@Component({
  selector: 'app-home-nav',
  imports: [SharedModule, RouterLink],
  templateUrl: './home-nav.html',
  styleUrl: './home-nav.css',
})
export class HomeNav {
    items: MenuItem[] | undefined;

    ngOnInit() {
        this.items = [
            {
                label: 'profile',
                path: 'profile'
            },
            {
                label: 'dashboard',
                path: 'dashboard'
            },
        ];
    }
}
