import axios from 'axios';
import { client } from './client';
import type { Staff } from '@/types/staff';

const BASE = import.meta.env.VITE_API_BASE_URL ?? '/api';

// client インスタンスの Content-Type: application/json を避けるため素の axios を使う
export const login = (data: { email: string; password: string }) => {
  const params = new URLSearchParams();
  params.append('username', data.email);
  params.append('password', data.password);
  return axios.post<{ message: string }>(`${BASE}/auth/sign-in`, params, {
    withCredentials: true,
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  }).then(r => r.data);
};

export const logout = () =>
  client.post('/auth/logout').then(r => r.data);

export const getMe = () =>
  client.get<Staff>('/auth/me').then(r => r.data);

export const changePassword = (data: {
  currentPassword: string;
  newPassword: string;
}) => client.post('/auth/password/change', data).then(r => r.data);
