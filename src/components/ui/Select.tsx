import { ChevronDown, Search } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { createPortal } from 'react-dom';
import { useTranslation } from '../../contexts/LanguageContext';

export interface SelectOption {
  value: string;
  label: string;
}

interface SelectProps {
  value: string;
  onChange: (value: string) => void;
  options: SelectOption[];
  placeholder?: string;
  searchable?: boolean;
  searchPlaceholder?: string;
  emptyLabel?: string;
  className?: string;
}

export function Select({
  value,
  onChange,
  options,
  placeholder,
  searchable = false,
  searchPlaceholder,
  emptyLabel,
  className = '',
}: SelectProps) {
  const { t } = useTranslation();
  const placeholderText = placeholder ?? t('common.select.placeholder');
  const searchPlaceholderText = searchPlaceholder ?? t('common.select.search');
  const emptyLabelText = emptyLabel ?? t('common.select.empty');
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState('');
  const [panelPos, setPanelPos] = useState({ top: 0, left: 0, width: 0 });
  const triggerRef = useRef<HTMLButtonElement>(null);
  const panelRef = useRef<HTMLDivElement>(null);

  // Position the portaled panel right under the trigger and follow scroll/resize.
  useEffect(() => {
    if (!open || !triggerRef.current) return;
    const updatePos = () => {
      const rect = triggerRef.current!.getBoundingClientRect();
      setPanelPos({
        top: rect.bottom + 8,
        left: rect.left,
        width: rect.width,
      });
    };
    updatePos();
    window.addEventListener('scroll', updatePos, true);
    window.addEventListener('resize', updatePos);
    return () => {
      window.removeEventListener('scroll', updatePos, true);
      window.removeEventListener('resize', updatePos);
    };
  }, [open]);

  // Close on outside click (not in trigger and not in portaled panel) or Escape.
  useEffect(() => {
    if (!open) return;
    const onPointer = (e: MouseEvent) => {
      const target = e.target as Node;
      if (
        triggerRef.current && triggerRef.current.contains(target)
      ) return;
      if (panelRef.current && panelRef.current.contains(target)) return;
      setOpen(false);
      setSearch('');
    };
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        setOpen(false);
        setSearch('');
      }
    };
    document.addEventListener('mousedown', onPointer);
    document.addEventListener('keydown', onKey);
    return () => {
      document.removeEventListener('mousedown', onPointer);
      document.removeEventListener('keydown', onKey);
    };
  }, [open]);

  const filtered =
    searchable && search.trim()
      ? options.filter((o) =>
          o.label.toLowerCase().includes(search.trim().toLowerCase())
        )
      : options;

  const selected = options.find((o) => o.value === value);

  return (
    <div className={`relative ${className}`}>
      <button
        ref={triggerRef}
        type="button"
        onClick={() => setOpen((prev) => !prev)}
        aria-haspopup="listbox"
        aria-expanded={open}
        className="w-full pl-4 pr-12 py-3.5 rounded-xl border-2 border-gray-200 bg-white text-left
          hover:border-primary-300 focus:border-primary-500 focus:ring-4 focus:ring-primary-100
          transition-all duration-200 outline-none flex items-center
          dark:bg-surface-850 dark:border-surface-700 dark:hover:border-primary-400
          dark:focus:border-primary-400 dark:focus:ring-primary-900/40"
      >
        <span
          className={`truncate ${selected ? 'text-dark-800 dark:text-dark-50' : 'text-dark-400 dark:text-dark-400'}`}
        >
          {selected ? selected.label : placeholderText}
        </span>
        <ChevronDown
          size={20}
          strokeWidth={1.75}
          className={`absolute right-4 top-1/2 -translate-y-1/2 text-dark-500 dark:text-dark-300 transition-transform duration-200 ${
            open ? 'rotate-180' : ''
          }`}
        />
      </button>

      {open &&
        createPortal(
          <div
            ref={panelRef}
            style={{
              position: 'fixed',
              top: panelPos.top,
              left: panelPos.left,
              width: panelPos.width,
              zIndex: 1000,
            }}
            className="bg-white border-2 border-gray-100 rounded-xl shadow-xl max-h-80 flex flex-col overflow-hidden animate-fade-in dark:bg-surface-900 dark:border-surface-700 dark:shadow-black/50"
          >
            {searchable && (
              <div className="p-3 border-b border-gray-100 dark:border-surface-700 flex items-center gap-2">
                <Search
                  size={16}
                  strokeWidth={1.75}
                  className="text-dark-400 dark:text-dark-300 flex-shrink-0"
                />
                <input
                  type="text"
                  autoFocus
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  placeholder={searchPlaceholderText}
                  className="flex-1 outline-none text-sm bg-transparent placeholder:text-dark-400 dark:text-dark-50 dark:placeholder:text-dark-400"
                />
              </div>
            )}
            <div className="overflow-y-auto flex-1" role="listbox">
              {filtered.length === 0 ? (
                <div className="p-4 text-center text-dark-400 text-sm">
                  {emptyLabelText}
                </div>
              ) : (
                filtered.map((option) => (
                  <button
                    key={option.value}
                    type="button"
                    role="option"
                    aria-selected={option.value === value}
                    onClick={() => {
                      onChange(option.value);
                      setOpen(false);
                      setSearch('');
                    }}
                    className={`w-full text-left px-4 py-2.5 transition-colors text-dark-700 dark:text-dark-200 ${
                      option.value === value
                        ? 'bg-primary-50 font-semibold text-primary-700 dark:bg-primary-900/40 dark:text-primary-200'
                        : 'hover:bg-primary-50/60 dark:hover:bg-primary-900/30'
                    }`}
                  >
                    {option.label}
                  </button>
                ))
              )}
            </div>
          </div>,
          document.body
        )}
    </div>
  );
}
