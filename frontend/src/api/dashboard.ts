import { client } from './client';

export type DashboardData = {
  currentMonth: string;
  summary: { pending: number; sent: number; done: number };
  overdueMonths: { month: string; count: number }[];
  recentHistory: {
    id: number;
    officeName: string;
    userName: string;
    sendType: string;
    sentAt: string;
  }[];
};

export const getDashboard = () =>
  client.get<DashboardData>('/dashboard').then(r => r.data);
