import { Component } from '@angular/core';
import { HomeNav } from '../../components/home-nav/home-nav';
import { ChatWidget } from '../../components/chat-widget/chat-widget';
import { HomeFooter } from '../../components/home-footer/home-footer';
import { RouterOutlet } from '@angular/router';
import { SharedModule } from '../../shared/module/shared/shared-module';

@Component({
    selector: 'app-home-layout',
    imports: [HomeNav, HomeFooter, ChatWidget, RouterOutlet, SharedModule],
    templateUrl: './home-layout.html',
    styleUrl: './home-layout.css',
})
export class homeLayout {}
