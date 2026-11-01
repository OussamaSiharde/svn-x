package com.intellij.testFramework.vcs;

import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ChangeListManagerGate;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.Collection;
import java.util.List;

/** Test gate delegating to the real ChangeListManager. Not published in vcs-test-framework, so it lives here. */
public final class MockChangeListManagerGate implements ChangeListManagerGate {
  private final ChangeListManager myManager;

  public MockChangeListManagerGate(ChangeListManager manager) {
    myManager = manager;
  }

  @Override
  public List<LocalChangeList> getListsCopy() {
    return myManager.getChangeListsCopy();
  }

  @Override
  public LocalChangeList findChangeList(String name) {
    return myManager.findChangeList(name);
  }

  @Override
  public LocalChangeList addChangeList(String name, String comment) {
    return myManager.addChangeList(name, comment);
  }

  @Override
  public LocalChangeList findOrCreateList(String name, String comment) {
    LocalChangeList list = myManager.findChangeList(name);
    return list != null ? list : myManager.addChangeList(name, comment);
  }

  @Override
  public void editComment(String name, String comment) {
    myManager.editComment(name, comment);
  }

  @Override
  public void editName(String oldName, String newName) {
    myManager.editName(oldName, newName);
  }

  @Override
  public void setListsToDisappear(Collection<String> names) {
  }

  @Override
  public FileStatus getStatus(VirtualFile file) {
    return myManager.getStatus(file);
  }

  @Override
  public FileStatus getStatus(FilePath filePath) {
    return myManager.getStatus(filePath);
  }

  @Override
  public void setDefaultChangeList(String name) {
    myManager.setDefaultChangeList(name);
  }
}
