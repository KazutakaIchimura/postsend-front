import { z } from 'zod';

const baseStaffSchema = z.object({
  name: z.string().min(1, '氏名を入力してください').max(100),
  email: z.string()
    .min(1, 'メールアドレスを入力してください')
    .email('メールアドレスは ◯◯@◯◯.◯◯ の形で入力してください'),
  role: z.enum(['ADMIN', 'STAFF'] as const, { error: '権限を選んでください' }),
});

export const staffCreateSchema = baseStaffSchema.extend({
  password: z.string()
    .min(8, 'パスワードは8文字以上で、英字（a〜z）と数字（0〜9）をまぜて設定してください')
    .regex(/^(?=.*[a-zA-Z])(?=.*\d).+$/,
      'パスワードは8文字以上で、英字（a〜z）と数字（0〜9）をまぜて設定してください'),
});

export const staffEditSchema = baseStaffSchema;

export type StaffCreateForm = z.infer<typeof staffCreateSchema>;
export type StaffEditForm = z.infer<typeof staffEditSchema>;
