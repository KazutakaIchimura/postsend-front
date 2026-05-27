export type Role = 'ADMIN' | 'STAFF';

export type Staff = {
  id: number;
  name: string;
  email: string;
  role: Role;
  isActive: boolean;
  forcePasswordChange: boolean;
};
