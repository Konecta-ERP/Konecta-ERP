import { Component } from '@angular/core';
import { UserNav } from '../../components/user-nav/user-nav';
import { UserFooter } from '../../components/user-footer/user-footer';

@Component({
  selector: 'app-user-layout',
  imports: [UserNav,UserFooter],
  templateUrl: './user-layout.html',
  styleUrl: './user-layout.css',
})
export class UserLayout {

}
