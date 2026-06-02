import { Navigate, createBrowserRouter } from "react-router-dom";
import AppLayout from "./AppLayout";
import MerchantsListPage from "@/features/merchant/pages/MerchantsListPage";
import MerchantDetailPage from "@/features/merchant/pages/MerchantDetailPage";
import OnboardMerchantPage from "@/features/merchant/pages/OnboardMerchantPage";

/**
 * Route tree. Auth is enforced by <RequireAuth> in App.tsx (everything under AppLayout is private).
 * Feature modules register their routes here; keep them lazy as the app grows.
 */
export const router = createBrowserRouter([
  {
    path: "/",
    element: <AppLayout />,
    children: [
      { index: true, element: <Navigate to="/merchants" replace /> },
      { path: "merchants", element: <MerchantsListPage /> },
      { path: "merchants/new", element: <OnboardMerchantPage /> },
      { path: "merchants/:id", element: <MerchantDetailPage /> },
    ],
  },
]);
