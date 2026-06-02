import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { http } from "./http";

/** Standard list envelope returned by the API (PageResponse on the backend). */
export interface Page<T> {
  data: T[];
  page: { limit: number; number: number; totalElements: number; totalPages: number };
}

/**
 * Generic CRUD/list hooks over a REST resource, so every feature doesn't re-hand-write fetch code.
 * `resource` is the path segment, e.g. "devices", "deployments", "kyc-requests".
 */
export function makeResource<T>(resource: string) {
  const keys = {
    all: [resource] as const,
    list: (params: Record<string, unknown>) => [resource, "list", params] as const,
    detail: (id: string) => [resource, "detail", id] as const,
  };

  function useList(params: Record<string, unknown> = {}) {
    return useQuery({
      queryKey: keys.list(params),
      queryFn: async () =>
        (await http.get<Page<T>>(`/${resource}`, { params })).data,
    });
  }

  function useDetail(id: string) {
    return useQuery({
      queryKey: keys.detail(id),
      queryFn: async () => (await http.get<T>(`/${resource}/${id}`)).data,
      enabled: !!id,
    });
  }

  function useCreate() {
    const qc = useQueryClient();
    return useMutation({
      mutationFn: async (body: unknown) => (await http.post<T>(`/${resource}`, body)).data,
      onSuccess: () => qc.invalidateQueries({ queryKey: keys.all }),
    });
  }

  /** POST an action sub-resource, e.g. action(id, "complete", body) -> /resource/{id}:complete */
  function useAction() {
    const qc = useQueryClient();
    return useMutation({
      mutationFn: async ({ id, verb, body }: { id: string; verb: string; body?: unknown }) =>
        (await http.post<T>(`/${resource}/${id}:${verb}`, body ?? {})).data,
      onSuccess: () => qc.invalidateQueries({ queryKey: keys.all }),
    });
  }

  return { keys, useList, useDetail, useCreate, useAction };
}
