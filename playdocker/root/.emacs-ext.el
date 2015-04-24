
;;; ------------------------------------------
;;; Do not edit the generated file, as it has
;;; been generated, as a tangled file, by the
;;; fandifluous org-mode.
;;;
;;; Source: ~/Dropbox/emacs.d/dotemacs.org
;;; ------------------------------------------

;; Extra Packages

;;    Extra packages not available via the package manager go in my
;;    personal stash at: =$HOME/.emacs.d=

;; (add-to-list 'load-path "~/.emacs.d/")

;; Package Manager

;;    Emacs has become like every other operating system, and now has a
;;    [[http://tromey.com/elpa/][package manager]] with its own collection of repositories. Of
;;    course, now, instead of trying to figure out and maintain
;;    packages, we have to keep track of what packages live on what
;;    repos. This is [[http://batsov.com/articles/2012/02/19/package-management-in-emacs-the-good-the-bad-and-the-ugly/][an improvement]].

;;    *NB:* We want to add the [[http://marmalade-repo.org/][Marmalade repository]].

; (load "~/.emacs.d/elpa/package.el") Needed for version 23 only!

; list the packages you want
(setq package-list '(magit color-theme scala-mode2 auto-complete yasnippet ensime smex))

; list the repositories containing them
(setq package-archives '(("org"       . "http://orgmode.org/elpa/")
                         ("gnu"       . "http://elpa.gnu.org/packages/")
                         ("tromey"    . "http://tromey.com/elpa/")
                         ("melpa" . "http://melpa.milkbox.net/packages/")
                         ("marmalade" . "http://marmalade-repo.org/packages/")))

; activate all the packages (in particular autoloads)
(package-initialize)

; fetch the list of packages available 
(unless package-archive-contents
  (package-refresh-contents))

; install the missing packages
(dolist (package package-list)
  (unless (package-installed-p package)
    (package-install package)))

(package-initialize)


;; Variables

;;    General settings about me that other packages can use.

(setq user-mail-address "info@bitcoinprivacy.net")

;; Tabs vs Spaces

;;    I have learned to distrust tabs in my source code, so let's make
;;    sure that we only have spaces.

(setq-default indent-tabs-mode nil)
(setq tab-width 2)

;; Font Settings

;;    I love syntax highlighting.

(global-font-lock-mode 1)

;; Specify the default font as =Source Code Pro=, which should already
;;    be [[http://blogs.adobe.com/typblography/2012/09/source-code-pro.html][downloaded]] and installed.

;;(set-default-font "Source Code Pro")
;;(set-face-attribute 'default nil :font "Source Code Pro" :height 140)
;;(set-face-font 'default "Source Code Pro")

;;; Smex
(autoload 'smex "smex"
  "Smex is a M-x enhancement for Emacs, it provides a convenient interface to
your recently and most frequently used commands.")

(global-set-key (kbd "M-x") 'smex)

;; Color Theme

;;    We use the color theme project and followed [[http://www.nongnu.org/color-theme/][these instructions]].
;;    We now can do =M-x color-theme-<TAB> RET=

(require 'color-theme)

;; The color themes work quite well, except they don't know about the
;;    org-mode source code blocks, so we need to set up a couple
;;    functions that we can use to set them.

(defun org-src-color-blocks-light ()
  "Colors the block headers and footers to make them stand out more for lighter themes"
  (interactive)
  (custom-set-faces
   '(org-block-begin-line 
    ((t (:underline "#A7A6AA" :foreground "#008ED1" :background "#EAEAFF"))))
   '(org-block-background
     ((t (:background "#FFFFEA"))))
   '(org-block-end-line
     ((t (:overline "#A7A6AA" :foreground "#008ED1" :background "#EAEAFF")))))

   ;; Looks like the minibuffer issues are only for v23
   ; (set-face-foreground 'minibuffer "black")
   ; (set-face-foreground 'minibuffer-prompt "red")
)

(defun org-src-color-blocks-dark ()
  "Colors the block headers and footers to make them stand out more for dark themes"
  (interactive)
  (custom-set-faces
   '(org-block-begin-line 
     ((t (:foreground "#008ED1" :background "#002E41"))))
   '(org-block-background
     ((t (:background "#111111"))))
   '(org-block-end-line
     ((t (:foreground "#008ED1" :background "#002E41")))))

   ;; Looks like the minibuffer issues are only for v23
   ; (set-face-foreground 'minibuffer "white")
   ; (set-face-foreground 'minibuffer-prompt "white")
)

;; My main reason for wanting to use the color theme project is to
;;    switch between /black on white/ during the day, and /white on
;;    black/ at night.

(defun color-theme-my-default ()
  "Tries to set up a normal color scheme"
  (interactive)
  (color-theme-sanityinc-tomorrow-day)
  (org-src-color-blocks-light))

;; During the day, we use the "standard" theme:
(global-set-key (kbd "<f9> d") 'color-theme-my-default)

;; A good late-night scheme that isn't too harsh
(global-set-key (kbd "<f9> l") (lambda () (interactive)
                                 (color-theme-sanityinc-tomorrow-eighties)
                                 (org-src-color-blocks-dark)))

;; Programming late into the night
(global-set-key (kbd "<f9> m") (lambda () (interactive)
                                 (color-theme-sanityinc-tomorrow-bright)
                                 (org-src-color-blocks-dark)))

;; Too harsh? Use the late night theme
(global-set-key (kbd "<f9> n") (lambda () (interactive)
                                 (color-theme-sanityinc-tomorrow-night)
                                 (org-src-color-blocks-dark)))

;; IDO (Interactively DO Things)

;;     According to [[http://www.masteringemacs.org/articles/2010/10/10/introduction-to-ido-mode/][Mickey]], IDO is the greatest thing.

(setq ido-enable-flex-matching t)
(setq ido-everywhere t)
(ido-mode 1)

;; Backup Settings

;;     This setting moves all backup files to a central location.
;;     Got it from [[http://whattheemacsd.com/init.el-02.html][this page]].

(setq backup-directory-alist
      `(("." . ,(expand-file-name
                 (concat user-emacs-directory "backups")))))

;; Make backups of files, even when they're in version control

(setq vc-make-backup-files t)

;; Org-Mode Sprint Note Files

;;     At the beginning of each sprint, we need to set this to the new
;;     sprint file.

(setq current-sprint "2013-04")

(defun get-current-sprint-file ()
  (expand-file-name (concat "~/Dropbox/org/gilt/Sprint-" current-sprint ".org")))
(defvar current-sprint-file 
  (get-current-sprint-file)
  "The name of an Org mode that stores information about the current sprint.")

;; Changed the name of the sprint? Run:   (create-sprint-file)

;; When we change to a new sprint, we need to create a new Sprint
;;     Org File (from a template).

(defun create-sprint-file ()
  "Creates a new Sprint file"
  (interactive)
  (let (today (format-time-string "%Y-%m-%d %a"))
    (setq template (concat "#+TITLE:  Sprint " current-sprint "\n"
                  "#+AUTHOR: Howard Abrams\n"
                  "#+EMAIL:  habrams@gilt.com\n"
                  "#+DATE:   " today "\n\n"
                  "* My Work Issues\n\n"
                  "* Sprint Retrospective\n\n"))
    (with-temp-file current-sprint-file
      (insert template)
      (message (concat "Created " current-sprint-file)))))

;; Org-Mode Colors

;;   Before we load =org-mode= proper, we need to set the following
;;   syntax high-lighting parameters. These are used to help bring out
;;   the source code during literate programming mode.

;;   This information came from [[http://orgmode.org/worg/org-contrib/babel/examples/fontify-src-code-blocks.html][these instructions]], however, they tend
;;   to conflict with the /color-theme/, so we'll turn them off for now.

(defface org-block-begin-line
  '((t (:underline "#A7A6AA" :foreground "#008ED1" :background "#EAEAFF")))
  "Face used for the line delimiting the begin of source blocks.")

(defface org-block-background
  '((t (:background "#FFFFEA")))
  "Face used for the source block background.")

(defface org-block-end-line
  '((t (:overline "#A7A6AA" :foreground "#008ED1" :background "#EAEAFF")))
  "Face used for the line delimiting the end of source blocks.")

;; Library Loading

;;    The standard package manager (and most recent versions of Emacs)
;;    include =org-mode=, however, I want the latest version that has
;;    specific features for literate programming.

;;    Org-mode is installed in the global directory.

(add-to-list 'load-path "~/.emacs.d/org/lisp")
(require 'org)
; (require 'org-install)
(require 'ob-tangle)

;; Global Key Bindings for Org-Mode

;;    The =org-mode= has some useful keybindings that are helpful no
;;    matter what mode you are using currently.

(global-set-key "\C-cl" 'org-store-link)
(global-set-key "\C-ca" 'org-agenda)
(global-set-key "\C-cb" 'org-iswitchb)

;; Speed Keys

;;    If point is at the beginning of a headline or code block in
;;    org-mode, single keys do fun things. See =org-speed-command-help=
;;    for details (or hit the ? key at a headline).

(setq org-use-speed-commands t)

;; Specify the Org Directories

;;    I keep all my =org-mode= files in a few directories, and I would
;;    like them automatically searched when I generate agendas.

(setq org-agenda-files '("~/Dropbox/org/personal" 
                         "~/Dropbox/org/gilt" 
                         "~/Dropbox/org/lg" 
                         "~/Dropbox/org/rpg" 
                         "~/Dropbox/org/project"))

;; Auto Note Capturing

;;    Let's say you were in the middle of something, but would like to
;;    /take a quick note/, but without affecting the file you are
;;    working on. This is called a "capture", and is bound to the
;;    following key:

(define-key global-map "\C-cc" 'org-capture)

;; This will bring up a list of /note capturing templates/:

(setq org-capture-templates
      '(("t" "Thought or Note" plain (file "~/Dropbox/org/notes/GTD Thoughts.txt")
         "  * %i%?\n    %a")
        ("d" "General TODO Tasks" entry (file "~/Dropbox/org/notes/GTD Tasks.org")
         "* TODO %?\n  %i\n  %a" :empty-lines 1)
        ("g" "Interesting Gilt Link" entry (file+headline "~/Dropbox/org/gilt/General.org" "Links")
         "* %i%? :gilt:\n  Captured: %t\n  Linked: %a" :empty-lines 1)
        ("w" "Work Task" entry (file+headline "~/Dropbox/org/gilt/General.org" "Tasks")
         "* TODO %?%i :gilt:" :empty-lines 1)
        ("r" "Retrospective Note" entry (file+headline current-sprint-file "Sprint Retrospective")
         "* (Good/Bad) %i%? :gilt:\n  Sprint: %t\n  Linked: %a" :empty-lines 1)
        ("j" "Journal" entry (file+datetree "~/Dropbox/org/Journal Events.org")
         "* %?\nEntered on %U\n  %i\n  %a")))

;; General notes go into this file:
(setq org-default-notes-file "~/Dropbox/org/notes/GTD Tasks.org")

;; RSS Feeds to Notes

;;     A cool feature allows me to automatically take the tasks assigned
;;     to me during a Sprint, and create entries for me to add my
;;     personal notes and comments.

(setq org-feed-alist
      (list (list "Gilt Jira"
        "https://jira.gilt.com/sr/jira.issueviews:searchrequest-xml/15717/SearchRequest-15717.xml"
        (get-current-sprint-file) "My Work Issues")))
(setq org-feed-default-template "** %h\n   %a")
;; We really want to change the %h to %( replace ... \"%h\" and \"%a\" )
;; %(concat \"[[\%a][\" (substring \"%h\" 1) \"]\")

;; Checking Things Off

;;    When I check off an item as done, sometimes I want to add some
;;    details about the completion (this is really only helpful when I'm
;;    consulting). 

;;    With this setting, each time you turn an entry from a TODO state
;;    into the DONE state, a line ‘CLOSED: [timestamp]’ will be inserted
;;    just after the headline. If you turn the entry back into a TODO
;;    item through further state cycling, that line will be removed
;;    again.

; (setq org-log-done 'time)
(setq org-log-done 'note)

;; Org Publishing

;;    The brilliance of =org-mode= is the ability to publish your notes
;;    as HTML files into a web server. See [[http://orgmode.org/worg/org-tutorials/org-publish-html-tutorial.html][these instructions]].

;; (require 'org-publish)

(setq org-publish-project-alist  '(
  ("org-notes"
   :base-directory        "~/Dropbox/org/"
   :base-extension        "org"
   :publishing-directory  "~/Sites/"
   :recursive             t
   :publishing-function   org-publish-org-to-html
   :headline-levels       4             ; Just the default for this project.
   :auto-preamble         t
   :auto-sitemap          t             ; Generate sitemap.org automagically...
   :makeindex             t
   :section-numbers       nil
   :table-of-contents     nil
   :style "<link rel=\"stylesheet\" href=\"../css/styles.css\" type=\"text/css\"/><link rel=\"stylesheet\" href=\"css/styles.css\" type=\"text/css\"/> <script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js\" type=\"text/javascript\"></script> <link href=\"http://ajax.googleapis.com/ajax/libs/jqueryui/1.7.2/themes/smoothness/jquery-ui.css\" type=\"text/css\" rel=\"Stylesheet\" />    <script src=\"https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.min.js\" type=\"text/javascript\"></script> <script =\"text/jacascript\" src=\"js/script.js\"></script>"
   )

  ("org-static"
   :base-directory       "~/Dropbox/org/"
   :base-extension       "css\\|js\\|png\\|jpg\\|gif\\|pdf\\|mp3\\|ogg\\|swf"
   :publishing-directory "~/Sites/"
   :recursive            t
   :publishing-function  org-publish-attachment
   )

  ("all" :components ("org-notes" "org-static"))))

;; I really, really would like to affect the output of the
;;    exported/published HTML files to make them /prettier/.

(setq org-export-html-style "<link rel='stylesheet' href='http://www.howardism.org/styles/org-export-html-style.css' type='text/css'/>
<script src='http://use.edgefonts.net/source-sans-pro.js'></script>
<script src='http://use.edgefonts.net/source-code-pro.js'></script>")

;; And let's tie this to a keystroke to make it easier to use:

(global-set-key (kbd "<f9> p") 'org-export-as-s5)

;; The Tower of Babel

;;    The trick to literate programming is in the [[http://orgmode.org/worg/org-contrib/babel/intro.html][Babel project]], which
;;    allows org-mode to not only interpret source code blocks, but
;;    evaluate them and tangle them out to a file.

(org-babel-do-load-languages
 'org-babel-load-languages
 '((sh         . t)
   (js         . t)
   (emacs-lisp . t)
   (scala      . t)
   (clojure    . t)
   (dot        . t)
   (css        . t)
   (plantuml   . t)))

;; Font Coloring in Code Blocks
    
;;     Normally, fontifying the individual code blocks makes it
;;     impossible to work with, so instead of turning it on at the global
;;     level for all blocks, I created a couple of keystrokes to
;;     selectively colorize one block at a time.

; (setq org-src-fontify-natively t)

(global-set-key (kbd "<f9> g") 'org-src-fontify-buffer)
(global-set-key (kbd "<f9> f") 'org-src-fontify-block)

;; Clojure

;;    Me like Clojure, and since it is a LISP, then Emacs likes it too.

;; (require 'clojure-mode)

;; With the =elein= project installed, it allows us to do things
;;    like: =M-x elein-run-cmd koan run=

;;    The following makes [[https://github.com/weavejester/compojure/wiki][Compojure]] macro calls look better:

;;(define-clojure-indent
;;  (defroutes 'defun)
;;  (GET 2)
;;  (POST 2)
;;  (PUT 2)
;;  (DELETE 2)
;;  (HEAD 2)
;; (ANY 2)
;;  (context 2))

;; Paredit

;;     One of the cooler features of Emacs is the [[http://emacswiki.org/emacs/ParEdit][ParEdit mode]] which
;;     keeps all parenthesis balanced in Lisp-oriented languages.
;;     See this [[http://www.emacswiki.org/emacs/PareditCheatsheet][cheatsheet]].

;;(autoload 'paredit-mode "paredit"
;;  "Minor mode for pseudo-structurally editing Lisp code." t)

;; We need to associate specific language modes with ParEdit.
;;     We first create a helper function:

;;(defun turn-on-paredit () (paredit-mode 1))

;; Then associate the following Lisp-based modes with ParEdit:

;;(add-hook 'emacs-lisp-mode-hook       'turn-on-paredit)
;;(add-hook 'lisp-mode-hook             'turn-on-paredit)
;;(add-hook 'lisp-interaction-mode-hook 'turn-on-paredit)
;;(add-hook 'scheme-mode-hook           'turn-on-paredit)
;;(add-hook 'clojure-mode-hook          'turn-on-paredit)

;; Scala

;;    We need to load the [[https://github.com/haxney/scala-mode][scala mode]].
;;    We follow [[http://www.scala-lang.org/node/354][these instructions]] to hook it up with [[http://code.google.com/p/yasnippet/][Yasnippet]].

;; (require 'scala-mode2)

;; ;; Shouldn't this be done by default?
;; (add-to-list 'auto-mode-alist '("\\.scala$" . scala-mode))

;; (add-hook 'scala-mode-hook
;;           '(lambda ()
;;              (yas/minor-mode-on)
;;              (scala-mode-feature-electric-mode)))

;; JavaScript

;;    JavaScript should have three parts:
;;    - Syntax highlight (already included)
;;    - Syntax verification (with flymake-jshint)
;;    - Interactive REPL

;;    Why yes, it seems that the JavaScript mode has a special
;;    indentation setting. Go below?

(setq js-basic-indent 2)

(setq js2-basic-offset 2)
(setq js2-cleanup-whitespace t)
(setq js2-enter-indents-newline t)
(setq js2-global-externs "jQuery $")
(setq js2-indent-on-enter-key t)
(setq js2-mode-indent-ignore-first-tab t)

(autoload 'js2-mode "js2-mode" nil t)
(add-to-list 'auto-mode-alist '("\\.js$" . js2-mode))

;; Change the word "function" to just an "f":

(font-lock-add-keywords
 'js2-mode `(("\\(function *\\)("
             (0 (progn (compose-region (match-beginning 1) (match-end 1) "ƒ")
                       nil)))))

;; Place warning font around TODO and others:

(font-lock-add-keywords 'js2-mode
                        '(("\\<\\(FIX\\|TODO\\|FIXME\\|HACK\\|REFACTOR\\):"
                           1 font-lock-warning-face t)))

;; FlyMake and JSHint

;;    While editing JavaScript is baked into Emacs, it is kinda cool to
;;    have it give you red sections based on [[http://www.jshint.com/][jshint]].
;;    This is done with [[http://www.emacswiki.org/emacs/FlymakeJavaScript][FlyMake]].

;; Make sure that PATH can reference the 'jshint' executable:
;; (setenv "PATH" (concat "/usr/local/bin:/opt/local/bin:" (getenv "PATH")))
;; (setq exec-path '( "/usr/local/bin" "/usr/bin" "/opt/local/bin"))

;; (require 'flymake-jshint)
;; (add-hook 'js-mode-hook
;;           (lambda () (flymake-mode 1)))

;; Server JS with Node.js

;;     We use [[http://js-comint-el.sourceforge.net][js-comint]], but hook it up wi;; th node.js:

;; (require 'js-comint)
;; (setenv "NODE_NO_READLINE" "1")   ;; Turn off fancy node prompt
;; ;; Use node as our repl
;; (setq inferior-js-program-command "node")

;; ;; According to [[http://nodejs.org/api/all.html#all_repl][these instructions]], we set the =NODE_NO_READLINE=
;; ;;     variable.

;; ;;     Need some prompt configuration for the REPL:

;; (setq inferior-js-mode-hook
;;       (lambda ()
;;         ;; We like nice colors
;;         (ansi-color-for-comint-mode-on)
;;         ;; Deal with some prompt nonsense
;;         (add-to-list
;;          'comint-preoutput-filter-functions
;;          (lambda (output)
;;            (replace-regexp-in-string "\033\\[[0-9]+[GK]" "" output)
;;            (replace-regexp-in-string ".*1G.*3G" "&GT;" output)
;;            (replace-regexp-in-string "&GT;" "> " output)
;; ))))

;; ;; Now, we can start up a JavaScript node REPL: =run-js=

;; ;;     Let's test this out by loading this into a separate buffer (=C-c '=)
;; ;;     and then doing a =M-x send-buffer-and-go=.

;; ;;     Set up some helpful keyboard instructions:

;; (add-hook 'js2-mode-hook
;;         (lambda () 
;;           (local-set-key (kbd "C-x C-e") #'js-send-buffer-and-go)
;;           (local-set-key (kbd "C-x r")   #'run-js)))

;; ;; JSP

;; ;;     Dealing with [[http://www.emacswiki.org/emacs/JspMode][JSP files]] is bad. But we'll try the [[http://www.crossleys.org/~jim/work/jsp.el][jsp-mode]] first:

;; ; (autoload 'jsp-mode "jsp" "JSP" t)

;; ; Tell emacs to use jsp-mode for .jsp files
;; (add-to-list 'auto-mode-alist '("\\.jsp\\'" . html-mode))

;; Auto Complete

;;    This feature scans the code and suggests completions for what you
;;    are typing. Useful at times ... annoying at others.

(require 'auto-complete-config)
(add-to-list 'ac-dictionary-directories "~/.emacs.d/ac-dict")
(ac-config-default)

;; Yas Snippet

;;    The [[http://code.google.com/p/yasnippet/][yasnippet project]] allows me to create snippets of code that
;;    can be brought into a file, based on the language.

(add-to-list 'load-path "~/.emacs.d/yasnippet")
(require 'yasnippet)
(yas/global-mode 1)
; (yas/initialize)

;; We just have different directories for each:

(setq yas/snippet-dirs
      '("~/.emacs.d/snippets"            ;; personal snippets
        "~/.emacs.d/yasnippet/extras/imported"
        "~/Dropbox/emacs.d/snippets/javascript-mode"
        "~/Dropbox/emacs.d/snippets/clojure-mode"
        "~/Dropbox/emacs.d/snippets/org-mode"
        "~/Dropbox/emacs.d/snippets/emacs-list-mode"
        "~/.emacs.d/scala-emacs/contrib/yasnippet/snippets"))

; (mapc 'yas/load-directory yas-snippet-dirs)

;; Markdown

;;    Don't use Markdown nearly as much as I used to, but I'm surprised
;;    that the following extension-associations aren't the default:

(autoload 'markdown-mode "markdown-mode.el"
   "Major mode for editing Markdown files" t)
(add-to-list 'auto-mode-alist '("\\.md\\'" . markdown-mode))
(add-to-list 'auto-mode-alist '("\\.markdown\\'" . markdown-mode))
(add-to-list 'auto-mode-alist '("\\.txt\\'" . markdown-mode))

;; Note Grep

;;    I use the standard [[http://emacswiki.org/emacs/GrepMode#toc1][grep package]] in Emacs and wrap it so that I
;;    can easily search through my notes.

(defun ngrep (reg-exp)
  "Searches the Notes and ORG directory tree for an expression."
  (interactive "sSearch note directory for: ")
  (let ((file-ext "*.org *.md *.txt *.markdown")
        (search-dir "~/Dropbox/org"))
    (message "Searching in %s" search-dir)
    (grep-compute-defaults)
    (rgrep reg-exp file-ext search-dir)))

(define-key global-map "\C-x\C-n" 'ngrep)
(define-key global-map "\C-x\C-r" 'rgrep)

;; Don't forget that after doing a =C-x C-f= to find a file, you can
;;    hit another =M-f= to do a find the given directory (and subs).

;;    Also, you can do a full locate with =C-x C-l=:

(define-key global-map "\C-x\C-l" 'locate)
(setq locate-command "mdfind")  ;; Use Mac OS X's Spotlight

;; Then, we can use it like:

(setq ispell-personal-dictionary 
    (concat (getenv "HOME") "/Dropbox/dictionary-personal.txt"))

(dolist (hook '(text-mode-hook))
  (add-hook hook (lambda () (flyspell-mode 1))))
