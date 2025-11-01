import { NgModule } from '@angular/core';

import { CommonModule } from '@angular/common';
import {
    FormsModule,
    ReactiveFormsModule,
} from '@angular/forms';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { InputNumberModule } from 'primeng/inputnumber';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { Toast } from 'primeng/toast';
import { NgxSpinnerModule } from 'ngx-spinner';
import { MessageService } from 'primeng/api';
import { BadgeModule } from 'primeng/badge';
import { AvatarModule } from 'primeng/avatar';
import { Ripple } from 'primeng/ripple';
import { Menubar } from 'primeng/menubar';
import { Toolbar} from 'primeng/toolbar';
import { CardModule } from 'primeng/card';
@NgModule({
  declarations: [],
  imports: [
    MessageModule,
    ReactiveFormsModule,
    ButtonModule,
    FormsModule,
    InputGroupModule,
    InputGroupAddonModule,
    InputTextModule,
    SelectModule,
    InputNumberModule,
    CommonModule,
    Toast,
    NgxSpinnerModule,
    BadgeModule,
    AvatarModule,
    Ripple,
    Menubar,
    Toolbar,
    CardModule
    ],
    exports: [
    MessageModule,
    ReactiveFormsModule,
    ButtonModule,
    FormsModule,
    InputGroupModule,
    InputGroupAddonModule,
    InputTextModule,
    SelectModule,
    InputNumberModule,
    CommonModule,
    Toast,
    NgxSpinnerModule,
    BadgeModule,
    AvatarModule,
    Ripple,
    Menubar,
    Toolbar,
    CardModule
    ],
    providers: [MessageService]

})
export class SharedModule { }
