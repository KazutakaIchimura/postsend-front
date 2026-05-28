import { client } from './client';
import type { MailSend, MailSendByOffice, MailSendBatch } from '@/types/mailSend';

type RawMailSend = Omit<MailSend, 'isOverdue'> & { overdue?: boolean; isOverdue?: boolean };

const normalizeMailSend = (raw: RawMailSend): MailSend => ({
  ...raw,
  isOverdue: raw.isOverdue ?? raw.overdue ?? false,
});

export const getMailSendsByOffice = (status?: string) =>
  client.get<{ office: MailSendByOffice['office']; mailSends: RawMailSend[] }[]>('/mail-sends/by-office', { params: { status } })
    .then(r => r.data.map(g => ({ ...g, mailSends: g.mailSends.map(normalizeMailSend) })));

export const getMailSends = (params?: {
  status?: string;
  sendMonth?: string;
  userId?: number;
  officeId?: number;
  dateFrom?: string;
  dateTo?: string;
}) => client.get<RawMailSend[]>('/mail-sends', { params }).then(r => r.data.map(normalizeMailSend));

export const createMailSend = (data: {
  userId: number;
  officeId: number;
  sendType: string;
  sendMonth: string;
}) => client.post<MailSend>('/mail-sends', data).then(r => r.data);

export const createBatch = (data: {
  mailSendIds: number[];
  notes?: string;
}) => client.post<MailSendBatch>('/mail-send-batches', data).then(r => r.data);

export const exportMailSendsCsv = (params?: {
  dateFrom?: string;
  dateTo?: string;
  officeId?: number;
  userId?: number;
}) =>
  client.get<Blob>('/mail-sends/export', { params, responseType: 'blob' }).then(r => r.data);
