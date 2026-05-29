import { client } from './client';
import type { Staff, RoleOption } from '@/types/staff';

export const getStaffs = () =>
  client.get<Staff[]>('/staffs').then(r => r.data);

export const getRoles = () =>
  client.get<RoleOption[]>('/roles').then(r => r.data);

export const createStaff = (data: Omit<Staff, 'id' | 'isActive' | 'forcePasswordChange' | 'createdAt' | 'roleId'> & { password?: string }) =>
  client.post<Staff>('/staffs', data).then(r => r.data);

/**
 * スタッフ情報を更新する
 */
export const updateStaff = ({ id, data }: { id: number; data: Partial<Staff> }) =>
  client.put<Staff>(`/staffs/${id}`, data).then(r => r.data);
