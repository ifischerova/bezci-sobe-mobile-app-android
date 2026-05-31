import { describe, it, expect } from 'vitest';

// Utility function tests
describe('Validation Utilities', () => {
  const validateEmail = (email: string): boolean => {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  };

  const validateUsername = (username: string): boolean => {
    return /^[a-zA-Z0-9._-]+$/.test(username) && username.length >= 3;
  };

  const validatePassword = (password: string): boolean => {
    return password.length >= 6 && /(?=.*[a-z])(?=.*[A-Z])|(?=.*\d)/.test(password);
  };

  describe('Email Validation', () => {
    it('should accept valid email', () => {
      expect(validateEmail('test@example.com')).toBe(true);
      expect(validateEmail('user.name@domain.co.uk')).toBe(true);
    });

    it('should reject invalid email', () => {
      expect(validateEmail('invalid')).toBe(false);
      expect(validateEmail('invalid@')).toBe(false);
      expect(validateEmail('@domain.com')).toBe(false);
      expect(validateEmail('test@')).toBe(false);
    });
  });

  describe('Username Validation', () => {
    it('should accept valid username', () => {
      expect(validateUsername('user123')).toBe(true);
      expect(validateUsername('user_name')).toBe(true);
      expect(validateUsername('user-name')).toBe(true);
      expect(validateUsername('user.name')).toBe(true);
    });

    it('should reject invalid username', () => {
      expect(validateUsername('ab')).toBe(false); // too short
      expect(validateUsername('user name')).toBe(false); // space
      expect(validateUsername('user@name')).toBe(false); // invalid character
    });
  });

  describe('Password Validation', () => {
    it('should accept valid password', () => {
      expect(validatePassword('Password1')).toBe(true);
      expect(validatePassword('Pass123')).toBe(true);
      expect(validatePassword('password123')).toBe(true);
    });

    it('should reject invalid password', () => {
      expect(validatePassword('pass')).toBe(false); // too short
      expect(validatePassword('password')).toBe(false); // no uppercase or digit
    });
  });
});
