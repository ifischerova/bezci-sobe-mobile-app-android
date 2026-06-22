// Shared form-validation helpers used by the auth pages and covered by unit tests.

export const validateEmail = (email: string): boolean => {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
};

export const validateUsername = (username: string): boolean => {
  return /^[a-zA-Z0-9._-]+$/.test(username) && username.length >= 3;
};

export const validatePassword = (password: string): boolean => {
  return password.length >= 6 && /(?=.*[a-z])(?=.*[A-Z])|(?=.*\d)/.test(password);
};
