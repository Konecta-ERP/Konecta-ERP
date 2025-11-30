/**
 * System-wide role definitions
 * These roles correspond to the backend Role enum in identity-service
 */

/**
 * Define all roles in the system
 */
export const ROLES = {
    // Base roles
    EMP: 'EMP',
    MANAGER: 'MANAGER',

    // Department roles
    HR_EMP: 'HR_EMP',
    FINANCE_EMP: 'FINANCE_EMP',

    // HR roles
    HR_ASSOCIATE: 'HR_ASSOCIATE',
    HR_MANAGER: 'HR_MANAGER',

    // Finance roles
    ACCOUNTANT: 'ACCOUNTANT',
    CFO: 'CFO',

    // Admin role
    ADMIN: 'ADMIN',
} as const;

/**
 * Managerial roles - users with these roles have managerial permissions
 * Includes HR_MANAGER, MANAGER, and CFO
 */
export const MANAGERIAL_ROLES = new Set([
    ROLES.HR_MANAGER,
    ROLES.MANAGER,
    ROLES.CFO,
    ROLES.ADMIN, // Admin has all permissions
]);

/**
 * HR-specific roles with HR permissions
 */
export const HR_ROLES = new Set([ROLES.HR_MANAGER, ROLES.HR_ASSOCIATE, ROLES.ADMIN]);

/**
 * Finance-specific roles with finance permissions
 */
export const FINANCE_ROLES = new Set([ROLES.ACCOUNTANT, ROLES.CFO, ROLES.ADMIN]);

/**
 * Check if a user has managerial permissions
 * @param userRole The user's role
 * @returns true if the user is a manager, HR_MANAGER, CFO, or ADMIN
 */
export function isManagerialRole(userRole: string | undefined): boolean {
    return userRole ? MANAGERIAL_ROLES.has(userRole as any) : false;
}

/**
 * Check if a user has HR permissions
 * @param userRole The user's role
 * @returns true if the user is in HR_MANAGER, HR_ASSOCIATE, or ADMIN
 */
export function isHRRole(userRole: string | undefined): boolean {
    return userRole ? HR_ROLES.has(userRole as any) : false;
}

/**
 * Check if a user has Finance permissions
 * @param userRole The user's role
 * @returns true if the user is in ACCOUNTANT, CFO, or ADMIN
 */
export function isFinanceRole(userRole: string | undefined): boolean {
    return userRole ? FINANCE_ROLES.has(userRole as any) : false;
}

/**
 * Check if ADMIN - has universal access to all parts of the system
 * @param userRole The user's role
 * @returns true if the user is ADMIN
 */
export function isAdmin(userRole: string | undefined): boolean {
    return userRole === ROLES.ADMIN;
}

/**
 * Check if user can approve/reject requisitions (HR_MANAGER or ADMIN only)
 * @param userRole The user's role
 * @returns true if user can approve/reject requisitions
 */
export function canApproveRequisitions(userRole: string | undefined): boolean {
    return userRole === ROLES.HR_MANAGER || userRole === ROLES.ADMIN;
}

/**
 * Check if user can perform recruitment actions (all HR roles and ADMIN)
 * @param userRole The user's role
 * @returns true if user can perform recruitment actions
 */
export function canPerformRecruitmentActions(userRole: string | undefined): boolean {
    return userRole ? HR_ROLES.has(userRole as any) : false;
}

/**
 * Check if user can view and create requisitions (HR roles, managers, CFO, or ADMIN)
 * @param userRole The user's role
 * @returns true if user can view/create requisitions
 */
export function canViewAndCreateRequisitions(userRole: string | undefined): boolean {
    return userRole
        ? HR_ROLES.has(userRole as any) || MANAGERIAL_ROLES.has(userRole as any)
        : false;
}
