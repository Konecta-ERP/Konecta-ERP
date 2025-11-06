import { Component, Input } from '@angular/core';
import { SharedModule } from '../../shared/module/shared/shared-module';
import { IFeedbackResponse } from '../../core/interfaces/i-feedback-response';
@Component({
  selector: 'app-performance-review-card',
  imports: [SharedModule],
  templateUrl: './performance-review-card.html',
  styleUrl: './performance-review-card.css',
})
export class PerformanceReviewCard {
    @Input() review!: IFeedbackResponse;
}
