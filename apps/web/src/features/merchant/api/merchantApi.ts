import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { http } from "@/lib/http";
import type { Merchant, OnboardMerchantInput, PageResponse } from "../types";

const keys = {
  all: ["merchants"] as const,
  list: (q: string, page: number) => [...keys.all, "list", q, page] as const,
  detail: (id: string) => [...keys.all, "detail", id] as const,
};

export function useMerchants(q: string, page: number, limit = 50) {
  return useQuery({
    queryKey: keys.list(q, page),
    queryFn: async () => {
      const { data } = await http.get<PageResponse<Merchant>>("/merchants", {
        params: { q: q || undefined, page, limit },
      });
      return data;
    },
  });
}

export function useMerchant(id: string) {
  return useQuery({
    queryKey: keys.detail(id),
    queryFn: async () => (await http.get<Merchant>(`/merchants/${id}`)).data,
    enabled: !!id,
  });
}

export function useOnboardMerchant() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (input: OnboardMerchantInput) =>
      (await http.post<Merchant>("/merchants", input)).data,
    onSuccess: () => qc.invalidateQueries({ queryKey: keys.all }),
  });
}

export function useActivateMerchant() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (id: string) =>
      (await http.post<Merchant>(`/merchants/${id}:activate`)).data,
    onSuccess: (m) => qc.invalidateQueries({ queryKey: keys.detail(m.id) }),
  });
}
