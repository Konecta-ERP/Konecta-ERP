export interface IFeedbackResponse {
    id: string;
    giverId: string;
    giverName?: string;
    createdAt: Date;
    imageUrl?: string;
    feedback: string;
}
