package com.intellij.psi.impl;

import com.intellij.formatting.FormatterEx;
import com.intellij.formatting.FormatterImpl;
import com.intellij.ide.startup.CacheUpdater;
import com.intellij.ide.startup.FileContent;
import com.intellij.ide.startup.FileSystemSynchronizer;
import com.intellij.ide.startup.StartupManagerEx;
import com.intellij.lang.LanguageDialect;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.cache.CacheManager;
import com.intellij.psi.impl.cache.RepositoryManager;
import com.intellij.psi.impl.cache.impl.CacheUtil;
import com.intellij.psi.impl.cache.impl.CompositeCacheManager;
import com.intellij.psi.impl.cache.index.IndexCacheManagerImpl;
import com.intellij.psi.impl.file.impl.FileManager;
import com.intellij.psi.impl.file.impl.FileManagerImpl;
import com.intellij.psi.impl.search.PsiSearchHelperImpl;
import com.intellij.psi.impl.source.PostprocessReformattingAspect;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageManagerImpl;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class PsiManagerImpl extends PsiManagerEx implements ProjectComponent {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.PsiManagerImpl");

  private final Project myProject;

  private final FileManager myFileManager;
  private final PsiSearchHelperImpl mySearchHelper;
  private final CacheManager myCacheManager;
  private final PsiModificationTrackerImpl myModificationTracker;
  private final ResolveCache myResolveCache;
  private final CachedValuesManager myCachedValuesManager;

  private final List<PsiTreeChangePreprocessor> myTreeChangePreprocessors = new CopyOnWriteArrayList<PsiTreeChangePreprocessor>();
  private final List<PsiTreeChangeListener> myTreeChangeListeners = new CopyOnWriteArrayList<PsiTreeChangeListener>();
  private boolean myTreeChangeEventIsFiring = false;

  private final List<Runnable> myRunnablesOnChange = new CopyOnWriteArrayList<Runnable>();
  private final List<WeakReference<Runnable>> myWeakRunnablesOnChange = new CopyOnWriteArrayList<WeakReference<Runnable>>();
  private final List<Runnable> myRunnablesOnAnyChange = new CopyOnWriteArrayList<Runnable>();
  private final List<Runnable> myRunnablesAfterAnyChange = new CopyOnWriteArrayList<Runnable>();

  private boolean myIsDisposed;

  private VirtualFileFilter myAssertOnFileLoadingFilter = VirtualFileFilter.NONE;

  private final AtomicInteger myBatchFilesProcessingModeCount = new AtomicInteger(0);

  private static final Key<PsiFile> CACHED_PSI_FILE_COPY_IN_FILECONTENT = Key.create("CACHED_PSI_FILE_COPY_IN_FILECONTENT");
  public static final Key<LanguageDialect> LANGUAGE_DIALECT = Key.create("language.dialect");
  
  public static final int BEFORE_CHILD_ADDITION = 0;
  public static final int BEFORE_CHILD_REMOVAL = 1;
  public static final int BEFORE_CHILD_REPLACEMENT = 2;
  public static final int BEFORE_CHILD_MOVEMENT = 3;
  public static final int BEFORE_CHILDREN_CHANGE = 4;
  public static final int BEFORE_PROPERTY_CHANGE = 5;
  public static final int CHILD_ADDED = 6;
  public static final int CHILD_REMOVED = 7;
  public static final int CHILD_REPLACED = 8;
  public static final int CHILD_MOVED = 9;
  public static final int CHILDREN_CHANGED = 10;
  public static final int PROPERTY_CHANGED = 11;

  private final List<LanguageInjector> myLanguageInjectors = new CopyOnWriteArrayList<LanguageInjector>();
  private final ProgressManager myProgressManager;

  private final RepositoryManager myRepositoryManager;
  private final RepositoryElementsManager myRepositoryElementsManager;

  public PsiManagerImpl(Project project,
                        PsiManagerConfiguration psiManagerConfiguration,
                        final ProjectRootManagerEx projectRootManagerEx,
                        StartupManager startupManager,
                        FileTypeManager fileTypeManager,
                        VirtualFileManager virtualFileManager,
                        FileDocumentManager fileDocumentManager) {
    myProject = project;


    boolean isProjectDefault = project.isDefault();

    myFileManager = isProjectDefault ? new EmptyFileManager(this) : new FileManagerImpl(this, fileTypeManager,
                                                                                                    virtualFileManager, fileDocumentManager,
                                                                                                    projectRootManagerEx);
    mySearchHelper = new PsiSearchHelperImpl(this);
    final CompositeCacheManager cacheManager = new CompositeCacheManager();
    if (!isProjectDefault) {
      cacheManager.addCacheManager(new IndexCacheManagerImpl(this));
    }
    else {
      cacheManager.addCacheManager(new EmptyCacheManager());
    }
    final CacheManager[] managers = myProject.getComponents(CacheManager.class);
    for (CacheManager manager : managers) {
      cacheManager.addCacheManager(manager);
    }

    myCacheManager = cacheManager;

    myModificationTracker = new PsiModificationTrackerImpl(this);
    myTreeChangePreprocessors.add(myModificationTracker);
    myResolveCache = new ResolveCache(this);
    myCachedValuesManager = new CachedValuesManagerImpl(this);

    myRepositoryManager = psiManagerConfiguration.createRepositoryManager(this);
    myRepositoryElementsManager = psiManagerConfiguration.createRepositoryElementsManager(this, myRepositoryManager);

    if (startupManager != null) {
      ((StartupManagerEx)startupManager).registerPreStartupActivity(
        new Runnable() {
          public void run() {
            runPreStartupActivity();
          }
        }
      );
    }

    myProgressManager = ProgressManager.getInstance();
  }

  public void initComponent() {
  }

  public RepositoryManager getRepositoryManager() {
    if (myIsDisposed) {
      LOG.error("Project is already disposed.");
    }
    return myRepositoryManager;
  }

  public RepositoryElementsManager getRepositoryElementsManager() {
    return myRepositoryElementsManager;
  }

  public void disposeComponent() {
    myFileManager.dispose();
    myCacheManager.dispose();
    myRepositoryManager.dispose();

    myIsDisposed = true;
  }

  public boolean isDisposed() {
    return myIsDisposed;
  }

  public void dropResolveCaches() {
    myResolveCache.clearCache();
    physicalChange();
    nonPhysicalChange();
  }

  public boolean isInProject(@NotNull PsiElement element) {
    PsiFile file = element.getContainingFile();
    if (file instanceof PsiFileImpl && file.isPhysical() && file.getViewProvider().getVirtualFile() instanceof LightVirtualFile) return true;

    if (element instanceof PsiDirectoryContainer) {
      PsiDirectory[] dirs = ((PsiDirectoryContainer) element).getDirectories();
      for (PsiDirectory dir : dirs) {
        if (!isInProject(dir)) return false;
      }
      return true;
    }

    VirtualFile virtualFile = null;
    if (file != null) {
      virtualFile = file.getViewProvider().getVirtualFile();
    } else if (element instanceof PsiFileSystemItem) {
      virtualFile = ((PsiFileSystemItem)element).getVirtualFile();
    }

    if (virtualFile != null) {
      Module module = ModuleUtil.findModuleForFile(virtualFile, element.getProject());
      return module != null;
    }
    return false;
  }

  public void performActionWithFormatterDisabled(final Runnable r) {
    final PostprocessReformattingAspect component = getProject().getComponent(PostprocessReformattingAspect.class);
    try {
      ((FormatterImpl)FormatterEx.getInstance()).disableFormatting();
      component.disablePostprocessFormattingInside(new Computable<Object>() {
        public Object compute() {
          r.run();
          return null;
        }
      });
    }
    finally {
      ((FormatterImpl)FormatterEx.getInstance()).enableFormatting();
    }
  }

  public <T extends Throwable> void performActionWithFormatterDisabled(final ThrowableRunnable<T> r) throws T {
    final Throwable[] throwable = new Throwable[1];

    final PostprocessReformattingAspect component = getProject().getComponent(PostprocessReformattingAspect.class);
    try {
      ((FormatterImpl)FormatterEx.getInstance()).disableFormatting();
      component.disablePostprocessFormattingInside(new Computable<Object>() {
        public Object compute() { try { r.run(); } catch (Throwable t) { throwable[0] = t; } return null; }
      });
    }
    finally {
      ((FormatterImpl)FormatterEx.getInstance()).enableFormatting();
    }

    if (throwable[0] != null) //noinspection unchecked
      throw (T)throwable[0];
  }

  public <T> T performActionWithFormatterDisabled(Computable<T> r) {
    try {
      final PostprocessReformattingAspect component = PostprocessReformattingAspect.getInstance(getProject());
      ((FormatterImpl)FormatterEx.getInstance()).disableFormatting();
      return component.disablePostprocessFormattingInside(r);
    }
    finally {
      ((FormatterImpl)FormatterEx.getInstance()).enableFormatting();
    }
  }

  @NotNull
  public List<? extends LanguageInjector> getLanguageInjectors() {
    return myLanguageInjectors;
  }

  public void registerLanguageInjector(@NotNull LanguageInjector injector) {
    myLanguageInjectors.add(injector);
    InjectedLanguageManagerImpl.getInstanceImpl(myProject).psiManagerInjectorsChanged();
  }

  public void registerLanguageInjector(@NotNull final LanguageInjector injector, Disposable parentDisposable) {
    registerLanguageInjector(injector);
    Disposer.register(parentDisposable, new Disposable() {
      public void dispose() {
        unregisterLanguageInjector(injector);
      }
    });
  }

  public void unregisterLanguageInjector(@NotNull LanguageInjector injector) {
    myLanguageInjectors.remove(injector);
    InjectedLanguageManagerImpl.getInstanceImpl(myProject).psiManagerInjectorsChanged();
  }

  public ElementManipulatorsRegistry getElementManipulatorsRegistry() {
    return ReferenceProvidersRegistry.getInstance(myProject);
  }

  public void postponeAutoFormattingInside(Runnable runnable) {
    PostprocessReformattingAspect.getInstance(getProject()).postponeFormattingInside(runnable);
  }


  public void projectClosed() {
  }

  public void projectOpened() {
  }

  private void runPreStartupActivity() {
    if (LOG.isDebugEnabled()) {
      LOG.debug("PsiManager.runPreStartupActivity()");
    }
    myFileManager.runStartupActivity();

    myCacheManager.initialize();

    StartupManagerEx startupManager = StartupManagerEx.getInstanceEx(myProject);
    if (startupManager != null) {
      FileSystemSynchronizer synchronizer = startupManager.getFileSystemSynchronizer();

      if (PsiManagerConfiguration.getInstance().REPOSITORY_ENABLED) {
        synchronizer.registerCacheUpdater(myRepositoryManager.getCacheUpdater());
        CacheUpdater[] updaters = myCacheManager.getCacheUpdaters();
        for (CacheUpdater updater : updaters) {
          synchronizer.registerCacheUpdater(updater);
        }
      }
    }
  }

  public void setAssertOnFileLoadingFilter(VirtualFileFilter filter) {
    // Find something to ensure there's no changed files waiting to be processed in repository indicies.
    myRepositoryManager.updateAll();
    myAssertOnFileLoadingFilter = filter;
  }

  public boolean isAssertOnFileLoading(VirtualFile file) {
    return myAssertOnFileLoadingFilter.accept(file);
  }

  @NotNull
  public Project getProject() {
    return myProject;
  }

  public FileManager getFileManager() {
    return myFileManager;
  }

  public CacheManager getCacheManager() {
    if (myIsDisposed) {
      LOG.error("Project is already disposed.");
    }
    return myCacheManager;
  }

  @NotNull
  public CodeStyleManager getCodeStyleManager() {
    return CodeStyleManager.getInstance(myProject);
  }

  public ResolveCache getResolveCache() {
    myProgressManager.checkCanceled(); // We hope this method is being called often enough to cancel daemon processes smoothly
    return myResolveCache;
  }


  public boolean areElementsEquivalent(PsiElement element1, PsiElement element2) {
    myProgressManager.checkCanceled(); // We hope this method is being called often enough to cancel daemon processes smoothly

    if (element1 == element2) return true;
    if (element1 == null || element2 == null) {
      return false;
    }

    if (element1.equals(element2) ||
        element1.isEquivalentTo(element2) ||
        element2.isEquivalentTo(element1)
       ) {
      return true;
    }

    return false;
  }

  public PsiFile findFile(@NotNull VirtualFile file) {
    return myFileManager.findFile(file);
  }

  @Nullable
  public FileViewProvider findViewProvider(@NotNull VirtualFile file) {
    return myFileManager.findViewProvider(file);
  }

  @TestOnly
  public void cleanupForNextTest() {
    //myFileManager.cleanupForNextTest();
    LOG.assertTrue(ApplicationManager.getApplication().isUnitTestMode());
  }

  @Nullable
  public PsiFile getFile(FileContent content) {
    PsiFile psiFile = content.getUserData(CACHED_PSI_FILE_COPY_IN_FILECONTENT);
    if (psiFile == null) {
      final VirtualFile vFile = content.getVirtualFile();
      psiFile = myFileManager.getCachedPsiFile(vFile);
      if (psiFile == null) {
        psiFile = findFile(vFile);
        if (psiFile == null) return null;
        psiFile = CacheUtil.createFileCopy(content, psiFile);
      }
      //psiFile = content.putUserDataIfAbsent(CACHED_PSI_FILE_COPY_IN_FILECONTENT, psiFile);
      content.putUserData(CACHED_PSI_FILE_COPY_IN_FILECONTENT, psiFile);
    }

    LOG.assertTrue(psiFile instanceof PsiCompiledElement || psiFile.isValid());
    return psiFile;
  }

  public PsiDirectory findDirectory(@NotNull VirtualFile file) {
    myProgressManager.checkCanceled();

    return myFileManager.findDirectory(file);
  }


  public void invalidateFile(PsiFile file) {
    if (myIsDisposed) {
      LOG.error("Disposed PsiManager calls invalidateFile!");
    }

    final VirtualFile virtualFile = file.getVirtualFile();
    if (file.getViewProvider().isPhysical() && myCacheManager != null) {
      myCacheManager.addOrInvalidateFile(virtualFile);
    }
  }

  public void reloadFromDisk(@NotNull PsiFile file) {
    myFileManager.reloadFromDisk(file);
  }

  public void addPsiTreeChangeListener(@NotNull PsiTreeChangeListener listener) {
    myTreeChangeListeners.add(listener);
  }

  public void addPsiTreeChangeListener(@NotNull final PsiTreeChangeListener listener, Disposable parentDisposable) {
    addPsiTreeChangeListener(listener);
    Disposer.register(parentDisposable, new Disposable() {
      public void dispose() {
        removePsiTreeChangeListener(listener);
      }
    });
  }

  public void removePsiTreeChangeListener(@NotNull PsiTreeChangeListener listener) {
    myTreeChangeListeners.remove(listener);
  }

  public void beforeChildAddition(PsiTreeChangeEventImpl event) {
    event.setCode(BEFORE_CHILD_ADDITION);
    if (LOG.isDebugEnabled()) {
      LOG.debug(
        "beforeChildAddition: parent = " + event.getParent()
      );
    }
    fireEvent(event);
  }

  public void beforeChildRemoval(PsiTreeChangeEventImpl event) {
    event.setCode(BEFORE_CHILD_REMOVAL);
    if (LOG.isDebugEnabled()) {
      LOG.debug(
        "beforeChildRemoval: child = " + event.getChild()
        + ", parent = " + event.getParent()
      );
    }
    fireEvent(event);
  }

  public void beforeChildReplacement(PsiTreeChangeEventImpl event) {
    event.setCode(BEFORE_CHILD_REPLACEMENT);
    if (LOG.isDebugEnabled()) {
      LOG.debug(
        "beforeChildReplacement: oldChild = " + event.getOldChild()
        + ", parent = " + event.getParent()
      );
    }
    fireEvent(event);
  }

  public void beforeChildrenChange(PsiTreeChangeEventImpl event) {
    event.setCode(BEFORE_CHILDREN_CHANGE);
    if (LOG.isDebugEnabled()) {
      LOG.debug("beforeChildrenChange: parent = " + event.getParent());
    }
    fireEvent(event);
  }

  public void beforeChildMovement(PsiTreeChangeEventImpl event) {
    event.setCode(BEFORE_CHILD_MOVEMENT);
    if (LOG.isDebugEnabled()) {
      LOG.debug(
        "beforeChildMovement: child = " + event.getChild()
        + ", oldParent = " + event.getOldParent()
        + ", newParent = " + event.getNewParent()
      );
    }
    fireEvent(event);
  }

  public void beforePropertyChange(PsiTreeChangeEventImpl event) {
    event.setCode(BEFORE_PROPERTY_CHANGE);
    if (LOG.isDebugEnabled()) {
      LOG.debug(
        "beforePropertyChange: element = " + event.getElement()
        + ", propertyName = " + event.getPropertyName()
        + ", oldValue = " + event.getOldValue()
      );
    }
    fireEvent(event);
  }

  public void childAdded(PsiTreeChangeEventImpl event) {
    onChange(true);
    event.setCode(CHILD_ADDED);
    if (LOG.isDebugEnabled()) {
      LOG.debug(
        "childAdded: child = " + event.getChild()
        + ", parent = " + event.getParent()
      );
    }
    fireEvent(event);
    afterAnyChange();
  }

  public void childRemoved(PsiTreeChangeEventImpl event) {
    onChange(true);
    event.setCode(CHILD_REMOVED);
    if (LOG.isDebugEnabled()) {
      LOG.debug(
        "childRemoved: child = " + event.getChild() + ", parent = " + event.getParent()
      );
    }
    fireEvent(event);
    afterAnyChange();
  }

  public void childReplaced(PsiTreeChangeEventImpl event) {
    onChange(true);
    event.setCode(CHILD_REPLACED);
    if (LOG.isDebugEnabled()) {
      LOG.debug(
        "childReplaced: oldChild = " + event.getOldChild()
        + ", newChild = " + event.getNewChild()
        + ", parent = " + event.getParent()
      );
    }
    fireEvent(event);
    afterAnyChange();
  }

  public void childMoved(PsiTreeChangeEventImpl event) {
    onChange(true);
    event.setCode(CHILD_MOVED);
    if (LOG.isDebugEnabled()) {
      LOG.debug(
        "childMoved: child = " + event.getChild()
        + ", oldParent = " + event.getOldParent()
        + ", newParent = " + event.getNewParent()
      );
    }
    fireEvent(event);
    afterAnyChange();
  }

  public void childrenChanged(PsiTreeChangeEventImpl event) {
    onChange(true);
    event.setCode(CHILDREN_CHANGED);
    if (LOG.isDebugEnabled()) {
      LOG.debug(
        "childrenChanged: parent = " + event.getParent()
      );
    }
    fireEvent(event);
    afterAnyChange();
  }

  public void propertyChanged(PsiTreeChangeEventImpl event) {
    onChange(true);
    event.setCode(PROPERTY_CHANGED);
    if (LOG.isDebugEnabled()) {
      LOG.debug(
        "propertyChanged: element = " + event.getElement()
        + ", propertyName = " + event.getPropertyName()
        + ", oldValue = " + event.getOldValue()
        + ", newValue = " + event.getNewValue()
      );
    }
    fireEvent(event);
    afterAnyChange();
  }

  public void addTreeChangePreprocessor(PsiTreeChangePreprocessor preprocessor) {
    myTreeChangePreprocessors.add(preprocessor);
  }

  private void fireEvent(PsiTreeChangeEventImpl event) {
    boolean isRealTreeChange = event.getCode() != PROPERTY_CHANGED && event.getCode() != BEFORE_PROPERTY_CHANGE;

    PsiFile file = event.getFile();
    if (file == null || file.isPhysical()) {
      ApplicationManager.getApplication().assertWriteAccessAllowed();
    }
    if (isRealTreeChange) {
      LOG.assertTrue(!myTreeChangeEventIsFiring, "Changes to PSI are not allowed inside event processing");
      myTreeChangeEventIsFiring = true;
    }
    try {
      for(PsiTreeChangePreprocessor preprocessor: myTreeChangePreprocessors) {
        preprocessor.treeChanged(event);
      }

      for (PsiTreeChangeListener listener : myTreeChangeListeners) {
        try {
          switch (event.getCode()) {
            case BEFORE_CHILD_ADDITION:
              listener.beforeChildAddition(event);
              break;

            case BEFORE_CHILD_REMOVAL:
              listener.beforeChildRemoval(event);
              break;

            case BEFORE_CHILD_REPLACEMENT:
              listener.beforeChildReplacement(event);
              break;

            case BEFORE_CHILD_MOVEMENT:
              listener.beforeChildMovement(event);
              break;

            case BEFORE_CHILDREN_CHANGE:
              listener.beforeChildrenChange(event);
              break;

            case BEFORE_PROPERTY_CHANGE:
              listener.beforePropertyChange(event);
              break;

            case CHILD_ADDED:
              listener.childAdded(event);
              break;

            case CHILD_REMOVED:
              listener.childRemoved(event);
              break;

            case CHILD_REPLACED:
              listener.childReplaced(event);
              break;

            case CHILD_MOVED:
              listener.childMoved(event);
              break;

            case CHILDREN_CHANGED:
              listener.childrenChanged(event);
              break;

            case PROPERTY_CHANGED:
              listener.propertyChanged(event);
              break;
          }
        }
        catch (Exception e) {
          LOG.error(e);
        }
      }
    }
    finally {
      if (isRealTreeChange) {
        myTreeChangeEventIsFiring = false;
      }
    }
  }

  public void registerRunnableToRunOnChange(Runnable runnable) {
    myRunnablesOnChange.add(runnable);
  }

  public void registerWeakRunnableToRunOnChange(Runnable runnable) {
    myWeakRunnablesOnChange.add(new WeakReference<Runnable>(runnable));
  }

  public void registerRunnableToRunOnAnyChange(Runnable runnable) { // includes non-physical changes
    myRunnablesOnAnyChange.add(runnable);
  }

  public void registerRunnableToRunAfterAnyChange(Runnable runnable) { // includes non-physical changes
    myRunnablesAfterAnyChange.add(runnable);
  }

  public void nonPhysicalChange() {
    onChange(false);
  }

  public void physicalChange() {
    onChange(true);
  }

  private void onChange(boolean isPhysical) {
    if (isPhysical) {
      runRunnables(myRunnablesOnChange);

      WeakReference[] refs = myWeakRunnablesOnChange.toArray(
        new WeakReference[myWeakRunnablesOnChange.size()]);
      myWeakRunnablesOnChange.clear();
      for (WeakReference ref : refs) {
        Runnable runnable = ref != null ? (Runnable)ref.get() : null;
        if (runnable != null) {
          runnable.run();
        }
      }
    }

    runRunnables(myRunnablesOnAnyChange);
  }

  private void afterAnyChange() {
    runRunnables(myRunnablesAfterAnyChange);
  }

  private static void runRunnables(List<Runnable> runnables) {
    if (runnables.isEmpty()) return;
    //noinspection ForLoopReplaceableByForEach
    for (int i = 0; i < runnables.size(); i++) {
      runnables.get(i).run();
    }
  }

  @NotNull
  public PsiSearchHelper getSearchHelper() {
    return mySearchHelper;
  }

  @NotNull
  public PsiModificationTracker getModificationTracker() {
    return myModificationTracker;
  }

  @NotNull
  public CachedValuesManager getCachedValuesManager() {
    return myCachedValuesManager;
  }

  public void moveDirectory(@NotNull final PsiDirectory dir, @NotNull PsiDirectory newParent) throws IncorrectOperationException {
    checkMove(dir, newParent);

    try {
      dir.getVirtualFile().move(this, newParent.getVirtualFile());
    }
    catch (IOException e) {
      throw new IncorrectOperationException(e.toString(),e);
    }
  }

  public void moveFile(@NotNull final PsiFile file, @NotNull PsiDirectory newParent) throws IncorrectOperationException {
    checkMove(file, newParent);

    try {
      final VirtualFile virtualFile = file.getVirtualFile();
      assert virtualFile != null;
      virtualFile.move(this, newParent.getVirtualFile());
    }
    catch (IOException e) {
      throw new IncorrectOperationException(e.toString(),e);
    }
  }

  public void checkMove(@NotNull PsiElement element, @NotNull PsiElement newContainer) throws IncorrectOperationException {
    if (element instanceof PsiDirectoryContainer) {
      PsiDirectory[] dirs = ((PsiDirectoryContainer)element).getDirectories();
      if (dirs.length == 0) {
        throw new IncorrectOperationException();
      }
      else if (dirs.length > 1) {
        throw new IncorrectOperationException(
          "Moving of packages represented by more than one physical directory is not supported.");
      }
      checkMove(dirs[0], newContainer);
      return;
    }

    //element.checkDelete(); //move != delete + add
    newContainer.checkAdd(element);
    checkIfMoveIntoSelf(element, newContainer);
  }

  private static void checkIfMoveIntoSelf(PsiElement element, PsiElement newContainer) throws IncorrectOperationException {
    PsiElement container = newContainer;
    while (container != null) {
      if (container == element) {
        if (element instanceof PsiDirectory) {
          if (element == newContainer) {
            throw new IncorrectOperationException("Cannot move directory into itself.");
          }
          else {
            throw new IncorrectOperationException("Cannot move directory into its subdirectory.");
          }
        }
        else {
          throw new IncorrectOperationException();
        }
      }
      container = container.getParent();
    }
  }

  public void startBatchFilesProcessingMode() {
    myBatchFilesProcessingModeCount.incrementAndGet();
  }

  public void finishBatchFilesProcessingMode() {
    myBatchFilesProcessingModeCount.decrementAndGet();
    LOG.assertTrue(myBatchFilesProcessingModeCount.get() >= 0);
  }

  public boolean isBatchFilesProcessingMode() {
    return myBatchFilesProcessingModeCount.get() > 0;
  }

  @NotNull
  public String getComponentName() {
    return "PsiManager";
  }
}
