import { z } from "zod";

/** Mirrors the backend MerchantResponse. Replace with generated types once contracts:gen is wired. */
export interface Merchant {
  id: string;
  merchantNo: string;
  legalName: string;
  tradeName?: string;
  taxId?: string;
  category?: string;
  status: "ONBOARDING" | "PENDING_KYC" | "ACTIVE" | "SUSPENDED" | "CLOSED";
  riskTier?: "LOW" | "MEDIUM" | "HIGH";
  onboardedAt?: string;
  activatedAt?: string;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface PageResponse<T> {
  data: T[];
  page: { limit: number; number: number; totalElements: number; totalPages: number };
}

export const onboardMerchantSchema = z.object({
  merchantNo: z.string().min(1).max(40),
  legalName: z.string().min(1).max(200),
  tradeName: z.string().max(200).optional(),
  taxId: z.string().max(40).optional(),
  category: z.string().max(80).optional(),
});

export type OnboardMerchantInput = z.infer<typeof onboardMerchantSchema>;
