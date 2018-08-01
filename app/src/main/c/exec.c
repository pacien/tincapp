/*
 * tinc app, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2018 Pacien TRAN-GIRARD
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

#include <jni.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/wait.h>

static inline const char **to_string_array(JNIEnv *env, jobjectArray ja) {
  const int len = (*env)->GetArrayLength(env, ja);
  const char **ca = calloc((size_t) len + 1, sizeof(char *));

  for (int i = 0; i < len; ++i) {
    jstring jstr = (jstring) (*env)->GetObjectArrayElement(env, ja, i);
    ca[i] = (*env)->GetStringUTFChars(env, jstr, NULL);
  }

  ca[len] = NULL;
  return ca;
}

static inline void exec(const char **argcv) {
  execv(argcv[0], (char *const *) argcv);
  exit(1);
}

JNIEXPORT jint JNICALL
Java_org_pacien_tincapp_commands_Executor_forkExec(JNIEnv *env, jclass class, jobjectArray argcv) {
  pid_t pid = fork();
  switch (pid) {
    case 0:
      exec(to_string_array(env, argcv));
      return 0;

    default:
      return pid;
  }
}

JNIEXPORT jint JNICALL
Java_org_pacien_tincapp_commands_Executor_wait(JNIEnv *env, jclass class, jint pid) {
  int status;
  waitpid(pid, &status, 0);
  return WIFEXITED(status) ? WEXITSTATUS(status) : -1;
}
