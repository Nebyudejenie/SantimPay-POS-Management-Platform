import NotificationsIcon from "@mui/icons-material/Notifications";
import { Badge, IconButton, List, ListItem, ListItemText, Menu, Typography } from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { useAuth } from "react-oidc-context";
import { http } from "@/lib/http";

interface Notification {
  id: string;
  template: string;
  relatedType?: string;
  createdAt: string;
}

/**
 * Notification bell: polls the inbox + subscribes to the live SSE stream so new events appear
 * without refresh. (Uses fetch for SSE since EventSource can't send the Authorization header;
 * the token is passed as a query param the API can also accept, or via cookie behind the tunnel.)
 */
export function NotificationsBell() {
  const auth = useAuth();
  const [anchor, setAnchor] = useState<null | HTMLElement>(null);
  const [live, setLive] = useState(0);

  const { data, refetch } = useQuery({
    queryKey: ["notifications", "inbox"],
    queryFn: async () =>
      (await http.get<{ data: Notification[] }>("/notifications", { params: { unreadOnly: true, limit: 20 } })).data,
  });

  useEffect(() => {
    const token = auth.user?.access_token;
    if (!token) return;
    const base = import.meta.env.VITE_API_BASE_URL ?? "/api/v1";
    const ctrl = new AbortController();
    // Minimal SSE consumer via fetch streaming.
    fetch(`${base}/stream/notifications`, {
      headers: { Authorization: `Bearer ${token}`, Accept: "text/event-stream" },
      signal: ctrl.signal,
    })
      .then(async (res) => {
        const reader = res.body?.getReader();
        if (!reader) return;
        const dec = new TextDecoder();
        for (;;) {
          const { done, value } = await reader.read();
          if (done) break;
          if (dec.decode(value).includes("event:notification")) {
            setLive((n) => n + 1);
            void refetch();
          }
        }
      })
      .catch(() => {/* stream closed */});
    return () => ctrl.abort();
  }, [auth.user?.access_token, refetch]);

  const items = data?.data ?? [];
  const count = items.length + live;

  return (
    <>
      <IconButton color="inherit" onClick={(e) => setAnchor(e.currentTarget)}>
        <Badge badgeContent={count} color="error"><NotificationsIcon /></Badge>
      </IconButton>
      <Menu anchorEl={anchor} open={!!anchor} onClose={() => setAnchor(null)}>
        {items.length === 0 && <ListItem><ListItemText primary="No new notifications" /></ListItem>}
        <List dense sx={{ width: 320 }}>
          {items.map((n) => (
            <ListItem key={n.id}>
              <ListItemText primary={n.template} secondary={
                <Typography variant="caption">{n.relatedType} · {new Date(n.createdAt).toLocaleString()}</Typography>
              } />
            </ListItem>
          ))}
        </List>
      </Menu>
    </>
  );
}
