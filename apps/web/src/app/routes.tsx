import { Navigate, createBrowserRouter } from "react-router-dom";
import AppLayout from "./AppLayout";
import MerchantsListPage from "@/features/merchant/pages/MerchantsListPage";
import MerchantDetailPage from "@/features/merchant/pages/MerchantDetailPage";
import OnboardMerchantPage from "@/features/merchant/pages/OnboardMerchantPage";
import DashboardPage from "@/features/analytics/DashboardPage";
import DevicesPage from "@/features/device/DevicesPage";
import DeploymentsPage from "@/features/deployment/DeploymentsPage";
import KycPage from "@/features/kyc/KycPage";
import WorkflowsPage from "@/features/workflow/WorkflowsPage";
import TasksPage from "@/features/tasks/TasksPage";
import AdminUsersPage from "@/features/admin/AdminUsersPage";
import FollowUpsPage from "@/features/followup/FollowUpsPage";
import ReportsPage from "@/features/reports/ReportsPage";

/**
 * Route tree. Auth is enforced in App.tsx (everything under AppLayout is private). Every nav target
 * in AppLayout must have a matching route here, or React Router renders a 404.
 */
export const router = createBrowserRouter([
  {
    path: "/",
    element: <AppLayout />,
    children: [
      // Land on Merchants — every role has merchant:read, so no one hits an empty/forbidden home.
      { index: true, element: <Navigate to="/merchants" replace /> },
      { path: "dashboard", element: <DashboardPage /> },
      { path: "merchants", element: <MerchantsListPage /> },
      { path: "merchants/new", element: <OnboardMerchantPage /> },
      { path: "merchants/:id", element: <MerchantDetailPage /> },
      { path: "devices", element: <DevicesPage /> },
      { path: "deployments", element: <DeploymentsPage /> },
      { path: "kyc", element: <KycPage /> },
      { path: "workflows", element: <WorkflowsPage /> },
      { path: "tasks", element: <TasksPage /> },
      { path: "follow-ups", element: <FollowUpsPage /> },
      { path: "reports", element: <ReportsPage /> },
      { path: "admin/users", element: <AdminUsersPage /> },
      // Catch-all: send unknown paths to Merchants (universal read) rather than a hard 404.
      { path: "*", element: <Navigate to="/merchants" replace /> },
    ],
  },
]);
