import { client } from './client';
import type { Staff } from '@/types/staff';

export const login = (data: { email: string; password: string }) =>
  client.post<Staff>('/auth/login', data).then(r => r.data);

export const logout = () =>
  client.post('/auth/logout').then(r => r.data);

export const getMe = () =>
  client.get<Staff>('/auth/me').then(r => r.data);

export const changePassword = (data: {
  currentPassword: string;
  newPassword: string;
}) => client.post('/auth/password/change', data).then(r => r.data);
