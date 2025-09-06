// src/utils/cn.ts
import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

/**
 * Merges class names using clsx and tailwind-merge, resolving Tailwind CSS conflicts.
 * @param inputs - Class names or conditional objects (e.g., strings, arrays, or { 'class': condition }).
 * @returns A single string of merged class names optimized for Tailwind CSS.
 * @example
 * cn('bg-blue-500', { 'text-white': true, 'hidden': false }) // Returns 'bg-blue-500 text-white'
 */
export function cn(...inputs: ClassValue[]): string {
  return twMerge(clsx(inputs));
}