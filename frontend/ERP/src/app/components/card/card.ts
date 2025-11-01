import { Component, Input } from '@angular/core';
import { SharedModule } from '../../shared/module/shared/shared-module';

@Component({
  selector: 'app-card',
  imports: [SharedModule],
  templateUrl: './card.html',
  styleUrl: './card.css',
})
export class Card {
  @Input() title: string = 'Default Title';
  @Input() subtitle: string = 'Default Subtitle';
  @Input() imageUrl: string = 'https://primefaces.org/cdn/primeng/images/card-ng.jpg';
  @Input() route?: string;

  onCardClick() {
    if (this.route) {
      // Navigate to route or emit event
      console.log('Navigate to:', this.route);
    }
  }
}
